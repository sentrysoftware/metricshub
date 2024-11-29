BEGIN {

############################################### Value to be set manually ###############################################
    # LUN Discovery
    # LUN DISCOVERY,lunname;manufacturerID;logicalDeviceID;deviceWWN;state;AdditionalInformation;alivePathCount;LocalDeviceName;RemoteDeviceName
    print "DISCOVERY;LunOnline;Dell;1;WWN1;Online;Hardware Location Code: U78AB.001.WZSGGSC-P1-C5-T2-W5006016408E03079-L07;1;localhost;remotehost"
    print "DISCOVERY;LunUnknown;Intel;2;WWN2;Unknown;Hardware Location Code: U78AB.001.WZSGGSC-P1-C5-T2-W500601693EA04D2B-L0;2;bacon"
    print "DISCOVERY;LunOffline;Hitachi;3;WWN3;Offline;Hardware Location Code: U78AB.001.WZSGGSC-P1-C5-T2-W500601603CE010D5-L0;3;pc-hds;"
    print "DISCOVERY;LunNoPath;Dell;4;WWN4;Offline;Hardware Location Code: U78AB.001.WZSGGSC-P1-C5-T2-W500601603CE010D5-L0;0;;"

    # Collect
    # COLLECT;logicalDeviceID;state;alivePathCount;
    print "COLLECT;1;alive;1"
    print "COLLECT;2;Unknown;2"
    print "COLLECT;3;dead;3"
    print "COLLECT;4;alive;0"
}