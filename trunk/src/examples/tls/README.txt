ShootistTLS README
==================

This example is provided to illustrate the usage of the TLS transport with
JAIN-SIP. It it just the original Shootist example by M. Ranganathan, but
featuring TLS transport. This is intended to serve as an example of how
to write a SIP-over-TLS application with JAIN-SIP.

To summarize, the key points for using JAIN-SIP with TLS are:

- Create a ListeningPoint for TLS, just by specifying "tls" as the
transport.
- Create a SipProvider on the TLS ListeningPoint.
- If your application is also using TCP transport, bear in mind that TCP and
TLS cannot use the same port, since TLS is actually implemented over TCP.
By default, the port for TCP is 5060 whereas the port for TLS is 5061.
- If your application checks at any point for a valid transport string (UDP
and/or TCP), remember to add also a check for TLS.
- Be sure to supply a suitable security infrastructure to the JVM when using
TLS. For example, you can use a keystore generated with Sun's "keytool"
command. This example is bundled with a test keystore; for using it you must
add the certificate inside it to your trusted certificates (again, using
"keytool"), and the launch your application setting the following
properties:
  - javax.net.ssl.keyStore=testKeyStore
  - javax.net.ssl.keyStorePassword=testPass

If you have any questions, feel free to ask at nist-sip@antd.nist.gov

-- Daniel J. Martinez Manzano.
