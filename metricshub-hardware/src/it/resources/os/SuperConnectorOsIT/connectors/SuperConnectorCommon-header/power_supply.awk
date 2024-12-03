BEGIN {

############################################### Value to be set manually ###############################################
    # PowerSupply  Discovery
    # PowerSupply  DISCOVERY;PsId;PsDeviceId;AttachedToDeviceID;AttachedToDeviceType;PowerSupplyType;PowerSupplyPower;AdditionalInformation
    print "DISCOVERY;1;Power Supply 1;1;Enclosure;AC;1F4;Serial Number: 100350353 Part Number: 357+KPA0018540-1"
    print "DISCOVERY;2;Power Supply 2;1;Enclosure;AC;258;Serial Number: 100350347 Part Number: 357+KPA0018540-1"
    print "DISCOVERY;3;Power Supply 3;1;Enclosure;AC;2BC;Serial Number: Part Number:"

    # PowerSupply  Collect
    # PowerSupply  COLLECT;PsId;PsStatus;UsedWatt;UsedPercent
    print "COLLECT;1;ok;300;20;"
    print "COLLECT;2;degraded;400;30;"
    print "COLLECT;3;failed;450;;" #checks that engine will calculate if not specified
}