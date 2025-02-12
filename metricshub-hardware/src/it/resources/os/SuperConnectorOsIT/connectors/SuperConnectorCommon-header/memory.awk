BEGIN {

############################################### Value to be set manually ###############################################
    # Memory  Discovery
    # Memory  DISCOVERY;MemoryID;MemoryDisplayID;AttachedToDeviceID;AttachedToDeviceType;Vendor;Model;SerialNumber;Type;Size;AdditionalInformation
    print "DISCOVERY;1;Memory 1;1;Enclosure;DELL;MEM44;ER4521DFE;DDR4;32000;Slot: J0155"
    print "DISCOVERY;2;Memory 2;1;Enclosure;DELL;MEM44;ER4521DFE;DDR4;16000;Slot: J0300"
    print "DISCOVERY;3;Memory 3;1;Enclosure;DELL;MEM44;ER4521DFE;DDR4;16000;Slot: J0301"

    # Memory  Collect
    # Memory  COLLECT;MemoryID;MemStatus;PredictedFailure;ErrorStatus;LastError;ErrorCount
  print "COLLECT;1;ok;0;ok;Memory ok;0;"
  print "COLLECT;2;degraded;0;degraded;;;"
  print "COLLECT;3;failed;1;failed;Memory fail to retrieve status;5;"
}