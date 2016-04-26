all:
	latexmk -f main

pdf:
	latexmk -pdf -f main

clean:
	rm -f *.aux *.fdb_latexmk *.fls *.log *~ *.dvi *.pdf *.blg *.toc *.bbl *.run.xml
