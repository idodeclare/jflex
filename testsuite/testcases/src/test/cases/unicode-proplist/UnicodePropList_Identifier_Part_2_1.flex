%%

%unicode 2.1
%public
%class UnicodePropList_Identifier_Part_2_1

%type int
%standalone

%include ../../resources/common-unicode-binary-property-java

%%

\p{Identifier Part} { setCurCharPropertyValue(); }
[^] { }

<<EOF>> { printOutput(); return 1; }
