BEGIN {
	OFS = ";"
}

/mtu/ {
	mtu = $5
}

/state/ {
	state = $9
}

/RX:/ {
	getline
	rx_bytes = $1
	rx_packets = $2
	rx_errors = $3
	rx_dropped = $4
	rx_missed = $5
	rx_mcast = $6
}

/TX:/ {
	getline
	tx_bytes = $1
	tx_packets = $2
	tx_errors = $3
	tx_dropped = $4
	tx_carrier = $5
	tx_collsns = $6
}

/^[0-9]+:/ {
	if (NR > 1) {
		print iface, mtu, state, rx_bytes, rx_packets, rx_errors, rx_dropped, rx_missed, rx_mcast, tx_bytes, tx_packets, tx_errors, tx_dropped, tx_carrier, tx_collsns
	}
	split($2, a, ":")
	iface = a[1]
	mtu = state = rx_bytes = rx_packets = rx_errors = rx_dropped = rx_missed = rx_mcast = tx_bytes = tx_packets = tx_errors = tx_dropped = tx_carrier = tx_collsns = ""
}

END {
	print iface, mtu, state, rx_bytes, rx_packets, rx_errors, rx_dropped, rx_missed, rx_mcast, tx_bytes, tx_packets, tx_errors, tx_dropped, tx_carrier, tx_collsns
}

