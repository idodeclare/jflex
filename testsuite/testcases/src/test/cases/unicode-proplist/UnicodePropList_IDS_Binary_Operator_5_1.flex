%%

%unicode 5.1
%public
%class UnicodePropList_IDS_Binary_Operator_5_1

%type int
%standalone

%include ../../resources/common-unicode-all-binary-property-java

%%

\p{IDS_Binary_Operator} { setCurCharPropertyValue(); }
[^] { }

<<EOF>> { printOutput(); return 1; }
