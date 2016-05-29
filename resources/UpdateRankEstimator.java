package com.turborilla.net.tasks;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.UUID;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.QueryKeys;
import com.turborilla.net.Request;
import com.turborilla.net.protocol.request.AbstractRequest;
import com.turborilla.net.protocol.response.EmptyResponse;
import com.turborilla.net.store.BestScore;
import com.turborilla.net.store.Highscore;
import com.turborilla.net.store.RankEstimator;
import com.turborilla.net.store.ScoreDistributionBlob;
import com.turborilla.net.store.ScoreDistribution;
import com.turborilla.net.tasks.UpdateRankEstimator.UpdateRankEstimatorRequest;
import com.turborilla.net.util.RequestFailedException;
import com.turborilla.net.tasks.DeleteScoreDistributionBlobs.DeleteScoreDistributionBlobsRequest;

/**
 * @author tobias
 *
 * Recursive task.
 *
 */
@SuppressWarnings("serial")
public class UpdateRankEstimator extends AbstractTask<UpdateRankEstimatorRequest> {
	
	public static long maxRequestDuration = 1000 * 20; // maximum 20 second execution time
	public static int maxPageSize = 500;
	
	public static class UpdateRankEstimatorRequest extends AbstractRequest {
		public String boardId;
		public boolean lightweight;
		
		// Recursion parameters, don't set them manually
		public String cursor;
		public Long startIndex = 0L;
		public Long recursionIndex = 0L;
		public Long intervalIndex = 0L;
		public String progressUUID = UUID.randomUUID().toString();
	}
	
	@Override
	protected EmptyResponse post(final Request request, UpdateRankEstimatorRequest data) throws RequestFailedException, Throwable {
		require(data.boardId, "boardId");
		
		// We want to save the result to a RankEstimator
		RankEstimator rankEstimator = null;
		ScoreDistribution scoreDistribution = null;
		
		if (data.intervalIndex == 0L) {			
			// Start a new update
			rankEstimator = new RankEstimator(request.game, data.boardId, data.progressUUID);		
			scoreDistribution = new ScoreDistribution(request.game, data.boardId, data.progressUUID);
		} else {
			// it should be present, since this is not the first time around the loop			
			rankEstimator = ofy().load().type(RankEstimator.class).id(RankEstimator.makeIdFromData(request.game, data.boardId, data.progressUUID)).safe();
			scoreDistribution = ofy().load().type(ScoreDistribution.class).id(ScoreDistribution.makeIdFromData(request.game, data.boardId, data.progressUUID)).safe();
		}
		
		logger.info("Rank on " + data.boardId + ": Starting recursive call number " + data.recursionIndex + " at rank " + data.startIndex);
		
		// Measure time to know when to spawn another task
		long startRequestTime = System.currentTimeMillis();
		
		final long rankUpdateTime = rankEstimator.getLastUpdateTime().getTime();
		
		String gameBoard = Highscore.makeGameBoardFromData(request.game, data.boardId);
		long startRank = data.startIndex;
		long intervalIndex = data.intervalIndex;
		Cursor cursor = data.cursor != null ? Cursor.fromWebSafeString(data.cursor) : null;
		
		// Start looping and constructing the table of scores and ranks
		while (true) {
			int basePageSize = 10;
			double incrementMultiplier = 0.02d;
			if (data.boardId.startsWith("gumbler")) {
				if (startRank <= 100) {
					basePageSize = 1;
					incrementMultiplier = 0d;
				} else if (startRank <= 500) {
					basePageSize = (int)(startRank / 50);
				}
			}
			int pageSize = basePageSize + (int)(startRank * incrementMultiplier); // bigger interval at the bottom of the list
			pageSize = pageSize < maxPageSize ? pageSize : maxPageSize;

			logger.info("Rank on " + data.boardId + ": Starting interval number " + intervalIndex + " at rank " + startRank + " with page size " + pageSize);

			QueryKeys<Highscore> highscoreKeys = ofy().load().type(Highscore.class)
					.filter("gameBoard", gameBoard)
					.limit(pageSize)
					.order("score")
					.startAt(cursor)
					.keys();

			final QueryResultIterator<Key<Highscore>> iterator = highscoreKeys.iterator();

			// Iterate through them all to know if we are done, or to get the cursor if the time is up
			long endRank = startRank - 1;
			int count = 0;
			if (iterator.hasNext()) {
				Key<Highscore> highscoreKey = iterator.next();
				count++;

				LoadResult<Highscore> startScoreResult = ofy().load().key(highscoreKey);
				Highscore startScore;
				Highscore endScore;

				while (iterator.hasNext()) {
					highscoreKey = iterator.next();
					count++;
				}

				endRank = startRank + count - 1; // Do this since endRank is an index and not a length
				endScore = ofy().load().key(highscoreKey).safe();
				startScore = startScoreResult.safe();
				
				// Set BestScore if this is the first loop and recursion, 
				// since it's not reliable in itself if scores have been deleted
				if (intervalIndex == 0) {
					BestScore.checkAndSet(
							startScore.getGame(), 
							startScore.getBoard(), 
							startScore.getUserId(), 
							null,
							startScore.getScore(),
							startScore.getSecondaryScore());
				}

				// do stuff now that we know the scores and ranks
				if (startRank == 0L) {
					rankEstimator.addScore(startScore.getScore(), 0L, startScore.getUserId());
					scoreDistribution.addScore(startScore.getScore(), 0L, startScore.getUserId(), startScore.getBlob());
					
					if (data.lightweight && startScore.getBlob() != null) {
						// Take ownership of startScore.getBlob()						
						ScoreDistributionBlob ownership = new ScoreDistributionBlob(request.game, data.boardId, startScore.getBlob(), startScore.getId(), rankUpdateTime);
						ofy().save().entity(ownership);
					}
				}
				
				rankEstimator.addScore(endScore.getScore(), endRank, endScore.getUserId());
				scoreDistribution.addScore(endScore.getScore(), endRank, endScore.getUserId(), endScore.getBlob());
				
				if (data.lightweight && endScore.getBlob() != null) {
					// Take ownership of endScore.getBlob()
					ScoreDistributionBlob ownership = new ScoreDistributionBlob(request.game, data.boardId, endScore.getBlob(), endScore.getId(), rankUpdateTime);
					ofy().save().entity(ownership);					
				}				
			}
			
			if (count < pageSize) {
				if (endRank >= 0) {
					final long numberOfScores = endRank + 1;
					logger.info("Rank on " + gameBoard + ": We are done with the rank estimator update. There were " + numberOfScores + " scores and " + (intervalIndex+1) + " intervals done by " + (data.recursionIndex+1) + " tasks");
					// Save the finished version and delete the in-progress one
					final RankEstimator finalRankEstimator = new RankEstimator(rankEstimator, numberOfScores);
					final ScoreDistribution finalScoreDistribution = new ScoreDistribution(scoreDistribution, numberOfScores);
					final RankEstimator inProgressRankEstimator = rankEstimator;
					final ScoreDistribution inProgressScoreDistribution = scoreDistribution;
					final boolean lightweight = data.lightweight;
					final String boardId = data.boardId;					
					ofy().transact(new VoidWork() {
						public void vrun() {
							if (lightweight) {			
								final DeleteScoreDistributionBlobsRequest data = new DeleteScoreDistributionBlobsRequest();
								data.boardId = boardId;
								data.rankUpdateTime = rankUpdateTime;
								deferTask("tasks/deletescoredistributionblobs", "delete-score-distribution-blobs", request, data);
							}
							ofy().save().entities(finalRankEstimator, finalScoreDistribution);
							ofy().delete().entities(inProgressRankEstimator, inProgressScoreDistribution);
						}
					});
				} else {
					logger.warning("Rank on " + gameBoard + ": There were no scores, cannot update RankEstimator");
				}
				break;
			} 

	        // Loop over the next page
			startRank = endRank + 1;
			cursor = iterator.getCursor();
			intervalIndex++;
			
	        if (System.currentTimeMillis() - startRequestTime > maxRequestDuration) {
	        	// Time is up, save it and enqueue another task to continue (transaction)
	            final UpdateRankEstimatorRequest nextData = new UpdateRankEstimatorRequest();
	            nextData.boardId = data.boardId;
	            nextData.cursor = cursor.toWebSafeString();
	            nextData.recursionIndex = data.recursionIndex + 1;
	            nextData.startIndex = startRank;
	            nextData.intervalIndex = intervalIndex;
	            nextData.progressUUID = data.progressUUID;
				
	            logger.info("Rank on " + data.boardId + ": Enqueueing the next batch of rank estimator, recursion number " + nextData.recursionIndex);
				
				final RankEstimator inProgressRankEstimator = rankEstimator;
				final ScoreDistribution inProgressScoreDistribution = scoreDistribution;
				
	        	ofy().transact(new VoidWork() {
					@Override
					public void vrun() {
						ofy().save().entities(inProgressRankEstimator, inProgressScoreDistribution);
				        deferTask("updaterankestimator", "update-estimator", request, nextData);
					}
				});
	            break;
	        }
		}
		
		ofy().clear(); // Clear session cache

		return new EmptyResponse();
	}

	
	@Override
	protected Class<UpdateRankEstimatorRequest> getRequestType() {
		return UpdateRankEstimatorRequest.class;
	}

}
