Shows a simple third party call control scenario. To run this example

1. ant phoneb
2. ant phonea
3. ant controller
4. ant controllerlog

The scenario is :
Flow I


             A              Controller               B
             |(1) INVITE no SDP  |                   |
             |<------------------|                   |
             |(2) 200 offer1     |                   |
             |------------------>|                   |
             |                   |(3) INVITE offer1  |
             |                   |------------------>|
             |                   |(4) 200 OK answer1 |
             |                   |<------------------|
             |                   |(5) ACK            |
             |                   |------------------>|
             |(6) ACK answer1    |                   |
             |<------------------|                   |
             |(7) RTP            |                   |
             |.......................................|
             
             
BYE



             A              Controller               B
             |(1) BYE            |                   |
             |------------------>|                   |
             |(2) 200 OK         |                   |
             |<------------------|                   |
             |                   |(3) BYE            |
             |                   |------------------>|
             |                   |(4) 200 OK         |
             |                   |<------------------|



Flow 4
4.4.  Flow IV

1. ant phoneb
2. ant phonea
3. ant controller4
4. ant controllerlog


             A                 Controller4                  B
             |(1) INVITE offer1     |                      |
             |no media              |                      |
             |<---------------------|                      |
             |(2) 200 answer1       |                      |
             |no media              |                      |
             |--------------------->|                      |
             |(3) ACK               |                      |
             |<---------------------|                      |
             |                      |(4) INVITE no SDP     |
             |                      |--------------------->|
             |                      |(5) 200 OK offer2     |
             |                      |<---------------------|
             |(6) INVITE offer2'    |                      |
             |<---------------------|                      |
             |(7) 200 answer2'      |                      |
             |--------------------->|                      |
             |                      |(8) ACK answer2       |
             |                      |--------------------->|
             |(9) ACK               |                      |
             |<---------------------|                      |
             |(10) RTP              |                      |
             |.............................................|

                                Figure 4



Example Contributed by 

 Kathleen McCallum
