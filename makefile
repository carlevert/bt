all:
	latexmk -f main

pdf:
	latexmk -pdf -f main

clean:
	rm -f *.aux *.fdb_latexmk *.fls *.log *~ *.pdf *.bbl *.run.xml
