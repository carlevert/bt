all:
	latexmk -f main

pdf:
	latexmk -pdf -f main

clean:
	rm -f *.aux *.fdb_latexmk *.fls *.log *~ *.dvi *.pdf *.blg *.toc *.bbl *.run.xml ; cp umuthesis/umu-logo.pdf .

public: pdf
	scp main.pdf dv14cks@peppar.cs.umu.se:~/public_html/thesis.pdf
