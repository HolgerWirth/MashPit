#!/bin/bash
DIR="/home/pi/scripts/rrd/"
 
#define the desired colors for the graphs
INTEMP_COLOR="#CC0000"
OUTTEMP_COLOR="#0000FF"
 
#hourly
nice -n 19 rrdtool graph $DIR/freezer_36.png --start -129600 -w 600 -h 150 \
DEF:Liquid=$DIR/freezertemp.rrd:Liquid:AVERAGE \
AREA:Liquid$INTEMP_COLOR:"Beer Temperature" \
DEF:Air=$DIR/freezertemp.rrd:Air:AVERAGE \
LINE1:Air$OUTTEMP_COLOR:"Air Temperature"
 
#daily
nice -n 19 rrdtool graph $DIR/freezer_daily.png --start -1d -w 600 -h 150 \
DEF:Liquid=$DIR/freezertemp.rrd:Liquid:AVERAGE \
AREA:Liquid$INTEMP_COLOR:"Beer Temperature" \
DEF:Air=$DIR/freezertemp.rrd:Air:AVERAGE \
LINE1:Air$OUTTEMP_COLOR:"Air Temperature"
 
#weekly
nice -n 19 rrdtool graph $DIR/freezer_weekly.png --start -1w -w 600 -h 150 \
DEF:Liquid=$DIR/freezertemp.rrd:Liquid:AVERAGE \
DEF:Air=$DIR/freezertemp.rrd:Air:AVERAGE \
AREA:Liquid$INTEMP_COLOR:"Beer Temperature" \
LINE1:Air$OUTTEMP_COLOR:"Air Temperature"
 
#monthly
nice -n 19 rrdtool graph $DIR/freezer_monthly.png --start -1m -w 600 -h 150 \
DEF:Liquid=$DIR/freezertemp.rrd:Liquid:AVERAGE \
DEF:Air=$DIR/freezertemp.rrd:Air:AVERAGE \
AREA:Liquid$INTEMP_COLOR:"Beer Temperature" \
LINE1:Air$OUTTEMP_COLOR:"Air Temperature"
 
#yearly
nice -n 19 rrdtool graph $DIR/freezer_yearly.png --start -1y -w 600 -h 150 \
DEF:Liquid=$DIR/freezertemp.rrd:Liquid:AVERAGE \
DEF:Air=$DIR/freezertemp.rrd:Air:AVERAGE \
AREA:Liquid$INTEMP_COLOR:"Beer Temperature" \
LINE1:Air$OUTTEMP_COLOR:"Air Temperature"
