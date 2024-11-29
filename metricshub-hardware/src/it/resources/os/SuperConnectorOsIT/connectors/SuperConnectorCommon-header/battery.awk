BEGIN {

############################################### Value to be set manually ###############################################
    # BATTERY Discovery
    # BATTERY DISCOVERY;BatteryDeviceID;BatteryModel;BatteryVendor;BatterieDisplayID;AttachedToDeviceID;AttachedToDeviceType;AdditionalInformation
    print "DISCOVERY;1;MegaPack800;Tesla;Battery1;1;Enclosure;Location: Rack1"
    print "DISCOVERY;2;Power1000;Panasonic;Battery2;1;Enclosure;Location: Rack2"
    print "DISCOVERY;3;Power3000;Panasonic;Battery3;1;Enclosure;Location: "

    # BATTERY Collect
    # BATTERY COLLECT;BatteryDeviceID;BatteryStatus;BatteryCharge
    print "COLLECT;1;up;95;5"
    print "COLLECT;2;down;5;120"
    print "COLLECT;3;mid;50;60"
}