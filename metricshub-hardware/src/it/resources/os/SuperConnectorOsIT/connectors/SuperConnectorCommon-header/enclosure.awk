BEGIN {

############################################### Value to be set manually ###############################################

    # Enclosure DISCOVERY
    # Enclosure DISCOVERY;EnclosureID;EnclosureDisplayID;EnclosureVendor;EnclosureModel;EnclosureType;AdditionalInformation
    print "DISCOVERY;1;Enclosure 1;EMC;ZED;Storage;Machine ID: 00F6BA5A4C00 System ID: 8000121245100000 LPAR System ID: 8000121245100002"
    print "DISCOVERY;2;000297800620 1;EMC;VMAX250F;Storage;Serial number : 297800620"

    # Enclosure Collect
    # Enclosure COLLECT;EnclosureeID;EnclosureStatus;EnclosureConsumption;EnergyUsage;IntrusionStatus
    print "COLLECT;1;ok;210;;ok"
    print "COLLECT;2;degraded;9999;;failed"
}