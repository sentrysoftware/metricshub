#!/bin/sh
ERRORMESSAGE=`%{SUDO:/bin/dd}/bin/dd if=$1 of=/dev/null count=20 2>&1`
if [ "$?" = "0" ]; then
    /bin/echo "MSHW;1;OK;Working";
else
    ERRORMESSAGE=`/bin/echo $ERRORMESSAGE|/bin/awk -F: '($4 !~ /denied/ && $4 !~ /[Nn]o such file/) {print $4}'`
    if [ -z "$ERRORMESSAGE" ]; then
        /bin/echo "MSHW;1;UNKNOWN;Unknown Status";
    else
        /bin/echo "MSHW;1;ALARM;$ERRORMESSAGE";
    fi
fi