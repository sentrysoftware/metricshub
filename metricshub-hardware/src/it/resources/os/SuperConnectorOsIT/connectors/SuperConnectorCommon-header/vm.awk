BEGIN {
############################################### Value to be set manually ###############################################
    # VM  Discovery
    # VM  DISCOVERY;DeviceID;DisplayID;AttachedToDeviceID;AttachedToDeviceType;Hostname;AdditionalInformation1
    print "DISCOVERY;weil;weil-09;1;Enclosure;localname;Bay1"
    print "DISCOVERY;weil2;weil-05;1;Enclosure;127.0.0.1;Bay2"
    print "DISCOVERY;weil3;weil;1;Enclosure;thehostname;Bay2"

    # VM  Collect
    # VM  COLLECT;DeviceID;PowerShare;PowerState
    print "COLLECT;weil;7;SUSPENDED;"
    print "COLLECT;weil2;70;ON;"
    print "COLLECT;weil3;10;OFF;"
}