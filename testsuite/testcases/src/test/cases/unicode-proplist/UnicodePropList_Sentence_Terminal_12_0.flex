%%

%unicode 12.0
%public
%class UnicodePropList_Sentence_Terminal_12_0

%type int
%standalone

%include ../../resources/common-unicode-all-binary-property-java

%%

\p{Sentence_Terminal} { setCurCharPropertyValue(); }
[^] { }

<<EOF>> { printOutput(); return 1; }
