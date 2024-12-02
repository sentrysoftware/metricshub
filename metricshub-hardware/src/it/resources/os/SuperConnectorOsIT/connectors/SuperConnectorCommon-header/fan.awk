BEGIN {

############################################### Value to be set manually ###############################################

    # FAN DISCOVERY
    # FAN DISCOVERY;FanID;FanDisplayID;FanType;PercentWarningThreshold;PercentAlarmThreshold;AttachedToDeviceID;AttachedToDeviceTyp;AdditionalInformation
    print "DISCOVERY;1;FAN 1;CPU;;;1;Enclosure;Fan Module: A Slot: 0"
    print "DISCOVERY;2;FAN 2;Front;;;1;Enclosure;Fan Module: B Slot: 1"

    # FAN Collect
    # FAN COLLECT;FanID;FanStatus;FanSpeed;SpeedPercent
    print "COLLECT;1;ok;15000;5;"
    print "COLLECT;2;ok;5411;98;"
}