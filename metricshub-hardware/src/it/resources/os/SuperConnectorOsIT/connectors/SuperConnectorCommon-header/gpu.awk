BEGIN {

  # GPU  Discovery
  # GPU  DISCOVERY;DeviceID;DisplayID;Vendor;Model;DriverVersion;FirmwareVersion;SerialNumber;Size;Location
  print "DISCOVERY;GPUNvidia;Nvidia 3080ti rtx;Nvidia;3080 ti;458.52635;35135131;AZEAX12454;16384;Location: Computer;1;Enclosure"
  print "DISCOVERY;GPUAMD150;Amd 6800 xt;Amd;6800 xt;1q25q;1111;SER14L;8192;Location: PCI Express;1;Enclosure"
  print "DISCOVERY;GPUINTEL;Intel Xe HPG;Intel;Xe;Xe-HPG 96EU;128EU;DG2;8192;Location: PCI Express;1;Enclosure"

  # GPU  COLLECT
  # UsedTime;DecoderUsedTime;EncoderUsedTime;ReceivedBytes;TransmittedBytes
  print "COLLECT;GPUNvidia;ok;Normal;0;10;122;10;60;50;55;15000;37500;800;"
  print "COLLECT;GPUAMD150;failed;Overheat;1;13;154;150;150;150;18;"";"";654;"
  print "COLLECT;GPUINTEL;degraded;Idle;;1;1;;;;1000;;;0;15;20;25;30000000;40000000"

}