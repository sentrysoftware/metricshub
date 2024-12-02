BEGIN {

############################################### Value to be set manually ###############################################
    # LogicalDisk  Discovery
    # LogicalDisk  DISCOVERY;LDid;LDdisplayid;LDAttachedToDeviceID;LDAttachedToDeviceType;LDControllerNumber;LDSsize;RaidLevel;LDtyper;AdditionalInformation
    print "DISCOVERY;1;Pool 1;1;Enclosure;1;5;5;pool;Protection Policy: HDD+2d:1n1-4:bay2-6"
    print "DISCOVERY;2;Pool 2;1;Enclosure;1;2;5;pool;Protection Policy: HDD+2d:1n1-4:bay7-12"
    print "DISCOVERY;3;volume 1;2;Enclosure;1;15;5;volume;Protection Policy: "
    print "DISCOVERY;4;volume 1;1;Enclosure;1;30;5;volume;Protection Policy: "

    # LogicalDisk  Collect
    # LogicalDisk  COLLECT;LDid;LDstatus;UnallocatedSpace;ErrorCount
    print "COLLECT;1;ok;30000000000000000"
    print "COLLECT;2;ok;5000000000000000"
    print "COLLECT;3;ok;20000000000000000"
    print "COLLECT;4;ok;20000000000000000;5"

}