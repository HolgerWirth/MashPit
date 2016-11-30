#!/bin/bash
rrdtool create freezertemp.rrd --start N --step 300 \
DS:Air:GAUGE:1200:U:30 \
DS:Liquid:GAUGE:1200:U:10 \
RRA:AVERAGE:0.5:1:10800 \
RRA:AVERAGE:0.5:5:2016 \
RRA:AVERAGE:0.5:15:2880 \
RRA:AVERAGE:0.5:60:8760 
