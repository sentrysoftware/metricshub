BEGIN {FS=";";}
NF > 4 && $1 != "" {if ($4 ~ /^[0-9]+$/) {print "MSHW;" $1 ";" $2 ";" $3 ";" $4 ";" $5 ";"}
                       else  {print "MSHW;" $1 ";" $2 ";" $4 ";"    ";" $6 ";"}
                   }