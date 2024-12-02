BEGIN {

############################################### Value to be set manually ###############################################
    # Voltage  Discovery
    # Voltage  DISCOVERY;VoltId;VoltDisplayId;AttachedToDeviceID;AttachedToDeviceType;VoltageType;AdditionalInformation
    print "DISCOVERY;1;Voltage1;1;Enclosure;CPU;Location: Bay-2"
    print "DISCOVERY;2;Voltage2;1;Enclosure;GPU;Location: PCI-E"

    # Voltage  Collect
    # Voltage  COLLECT;VoltId;VoltStatus;VoltStatusInformation;Voltage
    print "COLLECT;1;ok;;2100"
    print "COLLECT;2;ok;;54841"
}