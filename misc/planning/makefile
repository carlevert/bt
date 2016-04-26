all:
	latexmk -f planning
clean:
	rm -f *.aux *.fdb_latexmk *.fls *.log *~ *.pdf *.bbl *.run.xml

pdf: clean all
	latexmk -f -pdf planning
