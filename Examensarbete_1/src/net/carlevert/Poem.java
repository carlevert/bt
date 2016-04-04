package net.carlevert;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class Poem {

	private static final Logger log = Logger.getLogger(Poem.class.getName());
	private static final String suffixFormat = "%06d";
	public static final String[] text = { "It is an ancient Mariner,",
			"And he stoppeth one of three.",
			"'By thy long beard and glittering eye,",
			"Now wherefore stopp'st thou me?",
			"The Bridegroom's doors are opened wide,", "And I am next of kin;",
			"The guests are met, the feast is set:",
			"May'st hear the merry din.'",
			"He holds him with his skinny hand,",
			"'There was a ship,' quoth he.",
			"'Hold off ! unhand me, grey-beard loon!'",
			"Eftsoons his hand dropt he.",
			"He holds him with his glittering eye-",
			"The Wedding-Guest stood still,",
			"And listens like a three years' child:",
			"The Mariner hath his will.", "The Wedding-Guest sat on a stone:",
			"He cannot choose but hear;",
			"And thus spake on that ancient man,", "The bright-eyed Mariner.",
			"'The ship was cheered, the harbour cleared,",
			"Merrily did we drop", "Below the kirk, below the hill,",
			"Below the lighthouse top.", "The Sun came up upon the left,",
			"Out of the sea came he !",
			"And he shone bright, and on the right", "Went down into the sea.",
			"Higher and higher every day,", "Till over the mast at noon-'",
			"The Wedding-Guest here beat his breast,",
			"For he heard the loud bassoon.",
			"The bride hath paced into the hall,", "Red as a rose is she;",
			"Nodding their heads before her goes", "The merry minstrelsy.",
			"The Wedding-Guest he beat his breast,",
			"Yet he cannot choose but hear;",
			"And thus spake on that ancient man,", "The bright-eyed Mariner.",
			"'And now the STORM-BLAST came, and he",
			"Was tyrannous and strong:",
			"He struck with his o'ertaking wings,",
			"And chased us south along.",
			"With sloping masts and dipping prow,",
			"As who pursued with yell and blow",
			"Still treads the shadow of his foe,",
			"And forward bends his head,",
			"The ship drove fast, loud roared the blast,",
			"The southward aye we fled.",
			"And now there came both mist and snow,",
			"And it grew wondrous cold:",
			"And ice, mast-high, came floating by,", "As green as emerald.",
			"And through the drifts the snowy clifts",
			"Did send a dismal sheen:", "Nor shapes of men nor beasts we ken-",
			"The ice was all between.", "The ice was here, the ice was there,",
			"The ice was all around:",
			"It cracked and growled, and roared and howled,",
			"Like noises in a swound !", "At length did cross an Albatross,",
			"Thorough the fog it came;", "As if it had been a Christian soul,",
			"We hailed it in God's name.", "It ate the food it ne'er had eat,",
			"And round and round it flew.",
			"The ice did split with a thunder-fit;",
			"The helmsman steered us through !",
			"And a good south wind sprung up behind;",
			"The Albatross did follow,", "And every day, for food or play,",
			"Came to the Mariner hollo!",
			"In mist or cloud, on mast or shroud,",
			"It perched for vespers nine;",
			"Whiles all the night, through fog-smoke white,",
			"Glimmered the white Moon-shine.'",
			"'God save thee, ancient Mariner!",
			"From the fiends, that plague thee thus!-",
			"Why look'st thou so?'-With my cross-bow", "I shot the ALBATROSS." };

	private int counter;
	
	private int numLines;

	public int getNumLines() {
		return numLines;
	}
	
	@Id
	Long id;

	private Date timestamp;

	private String line_00;
	private String line_01;
	private String line_02;
	private String line_03;
	private String line_04;
	private String line_05;
	private String line_06;
	private String line_07;
	private String line_08;
	private String line_09;

	private String line_10;
	private String line_11;
	private String line_12;
	private String line_13;
	private String line_14;
	private String line_15;
	private String line_16;
	private String line_17;
	private String line_18;
	private String line_19;

	private String line_20;
	private String line_21;
	private String line_22;
	private String line_23;
	private String line_24;
	private String line_25;
	private String line_26;
	private String line_27;
	private String line_28;
	private String line_29;

	private String line_30;
	private String line_31;
	private String line_32;
	private String line_33;
	private String line_34;
	private String line_35;
	private String line_36;
	private String line_37;
	private String line_38;
	private String line_39;

	private String line_40;
	private String line_41;
	private String line_42;
	private String line_43;
	private String line_44;
	private String line_45;
	private String line_46;
	private String line_47;
	private String line_48;
	private String line_49;

	public Poem() {
		copyValuesToFields();
		timestamp = new Date();
		counter = 0;
	}

	public Long getId() {
		return id;
	}
	
	public void mutate() {
		Field[] fields = Poem.class.getDeclaredFields();
		counter++;
		String suffix = String.format(suffixFormat,  counter);
		for (Field field : fields) {
			String fieldName = field.getName();
			if (fieldName.startsWith("line_"))
				try {
					Integer i = Integer.parseInt(fieldName.substring(5));
					field.set(this, text[i] + suffix);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.warning("Exception: " + e.getMessage());
				}
		}
	}

	private void copyValuesToFields() {
		String suffix = String.format(suffixFormat,  counter);
		Field[] fields = Poem.class.getDeclaredFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			if (fieldName.startsWith("line_")) {
				numLines++;
				
				try {
					Integer i = Integer.parseInt(fieldName.substring(5));
					field.set(this, text[i] + suffix);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.warning("Exception: " + e.getMessage());
				}
			}
		}
	}
	
	public boolean isValid() {
		Field[] fields = Poem.class.getDeclaredFields();
		String suffix = String.format(suffixFormat,  counter);
		for (Field field : fields) {
			String fieldName = field.getName();
			if (fieldName.startsWith("line_")) {
				try {
					Integer i = Integer.parseInt(fieldName.substring(5));
					String s1 = (String) field.get(this);
					if (!s1.equals(text[i] + suffix))
						return false;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.warning("Exception: " + e.getMessage());
				}
			}
		}
		return true;
	}
	
	public int getHealth() {
		return 0;
	}

	public String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		return sdf.format(timestamp);
	}
	
}
