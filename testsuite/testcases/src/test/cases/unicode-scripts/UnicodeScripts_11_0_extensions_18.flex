%%

%unicode 11.0
%public
%class UnicodeScripts_11_0_extensions_18

%type int
%standalone

%include ../../resources/common-unicode-all-enumerated-property-defined-values-only-java

%%

<<EOF>> { printOutput(); return 1; }
\p{Script_Extensions:Telugu} { setCurCharPropertyValue("Script_Extensions:Telugu"); }
[^] { }
