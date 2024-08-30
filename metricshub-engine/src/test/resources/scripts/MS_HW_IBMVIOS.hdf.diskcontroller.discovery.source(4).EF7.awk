$1 ~ /MSHW_DATE[0-9]+/ {mmdd = substr($1,10,4) ; yy = substr ($1,18,2)}
# Match the date
substr($2,1,4) == mmdd &&  substr($2,9,2) == yy { if ($5 in DeviceErrorCount == 0) {DeviceErrorCount[$5] = 1}
	                                                else {DeviceErrorCount[$5] = DeviceErrorCount[$5] + 1 }
	                                               }
END {for (id in DeviceErrorCount) {print "MSHW;" id ";" DeviceErrorCount[id] ";"} }
