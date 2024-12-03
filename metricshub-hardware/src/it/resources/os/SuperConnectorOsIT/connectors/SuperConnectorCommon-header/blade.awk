BEGIN {

############################################### Value to be set manually ###############################################
    # BLADE Discovery
    # BLADE DISCOVERY;BaldeID;BladeDisplayID;BladeSerial;AttachedToDeviceID;AttachedToDeviceType;AdditionalInformation
    print "DISCOVERY;1;Blade1;15E4582A;1;Enclosure;Model Number: GVAX57A1-BNNX14X Partition: 0"
    print "DISCOVERY;2;Blade2;15A4586Z;1;Enclosure;Model Number: GVAX57A1-CNNX14Y Partition: 4"
    print "DISCOVERY;3;Blade3;17B4533A;1;Enclosure;Model Number: GVAX57A1-CNNX14Y Partition: 4"

    # BLADE Collect
    # BLADE COLLECT;BladeID;BladeStatus;PowerState
    print "COLLECT;1;ok;ON;"
    print "COLLECT;2;failed;SUSPENDED;"
    print "COLLECT;3;degraded;OFF;"
}