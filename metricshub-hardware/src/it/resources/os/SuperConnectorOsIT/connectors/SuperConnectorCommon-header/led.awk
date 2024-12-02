BEGIN {

############################################### Value to be set manually ###############################################
    # LED Discovery
    # LED DISCOVERY;LedID;LedDisplayId;AttachedToDeviceID;AttachedToDeviceType;Name;OnStatus;OffStatus;BlinkingStatus;AdditionalInformation
    print "DISCOVERY;1;LED1;1;Enclosure;Enclosure 1 Indicator 1;degraded;ok;failed;Color: RED and YELLOW"
    print "DISCOVERY;2;LED2;1;Enclosure;Enclosure 1 Indicator 2;degraded;ok;failed;Color: RED and YELLOW"

    # Collect
    # COLLECT;LedID;LedStatus;LedColor
    print "COLLECT;1;ON"
    print "COLLECT;2;OFF"
}