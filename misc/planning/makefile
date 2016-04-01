all:
	latexmk planning -f
clean:
	rm -f *.aux *.fdb_latexmk *.fls *.log *~ *.pdf *.bbl *.run.xml

pdf: clean all
	dvipdf planning.dvi
