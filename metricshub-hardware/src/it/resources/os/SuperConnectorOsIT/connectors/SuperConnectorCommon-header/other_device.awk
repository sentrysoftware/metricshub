BEGIN {

############################################### Value to be set manually ###############################################
    # OtherDevice  Discovery
    # OtherDevice  DISCOVERY;OdId;OdDisplayId;AttachedToDeviceID;AttachedToDeviceType;AdditionalInformation
    print "DISCOVERY;1;erssOtherDevices1887;1;Enclosure;Manufacturer:LSI - Model:9205-8e Serial Number:Onboard Firmware Version: 17.11.00.00"
    print "DISCOVERY;2;azzeOtherDevices2887;1;Enclosure;Part Number: GV-BE2LSW1X1-Y Serial Number: 002212 Model Number: "
    print "DISCOVERY;3;zereOtherDevices3887;1;Enclosure;Part Number: GVX-BE2MNG1X1 Serial Number: H9900652C04 Model Number: HBS03-BC"

    # OtherDevice  Collect
    # OtherDevice  COLLECT;OdId;OdStatus;OdStatusInformation;OdUsageCount;OdPowerConsumption;Value
    print "COLLECT;1;ok;Working fine;2300;15;99999;"
    print "COLLECT;2;degraded;Atention ralentissements;50;10;777;"
    print "COLLECT;3;failed;Les commandes ne repondent plus;120;50;;"
}