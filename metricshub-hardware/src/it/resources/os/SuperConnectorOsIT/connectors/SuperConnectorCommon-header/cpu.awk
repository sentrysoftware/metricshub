BEGIN {

############################################### Value to be set manually ###############################################
    # CPU Discovery
    # CPU DISCOVERY;CPUid;CPUdisplayID;CPUvendor;CPUmodel;CPUconso;CPUmaxSpeed;AdditionalInformation
    print "DISCOVERY;1;PROC_1;INTEL;Xeon E7;200;3000;1;Enclosure;Model: 45 (Intel Xeon CPU E5-2428L 0 @ 1.80GHz) "
    print "DISCOVERY;2;PROC_2;INTEL;Xeon E5;220;2800;1;Enclosure;Model: 45 (Intel Xeon CPU E5-2428L 0 @ 1.80GHz) "
    print "DISCOVERY;3;PROC_3;AMD;EPYC 7601;180;3200;1;Enclosure;Model: 45 (Intel Xeon CPU E5-2428L 0 @ 1.80GHz) "

    # CPU Collect
    # CPU COLLECT;CPUid;CPUstatus;CPUcurrSpeed;CorrectedErrorCount;PredictedFailure
    print "COLLECT;1;ok;1800;5;0"
    print "COLLECT;2;failed;3000;10;1"
    print "COLLECT;3;degraded;2200;15;"
}