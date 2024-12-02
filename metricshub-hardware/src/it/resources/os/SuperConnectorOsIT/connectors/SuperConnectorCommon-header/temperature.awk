BEGIN {

############################################### Value to be set manually ###############################################
    # Temperature  Discovery
    # Temperature  DISCOVERY;TempId;TempDisplayId;AttachedToDeviceID;AttachedToDeviceType;TemperatureType;WarningThreshold;AlarmThreshold;AdditionalInformation
    print "DISCOVERY;1;TEMP 1;1;Enclosure;CPU;60;80;Ambient: PSU-1"
    print "DISCOVERY;2;TEMP 2;1;Enclosure;CPU;60;80;Ambient: PSU-2"

    # Temperature  Collect
    # Temperature  COLLECT;TempId;TempStatus;TempStatusInformation;Temperature
    print "COLLECT;1;ok;Temp ok;38"
    print "COLLECT;2;ok;Temp ok;98"
}