package test.tck.factory;
import javax.sip.address.*;
import junit.framework.*;
import test.tck.*;

public class AddressFactoryTest extends FactoryTestHarness {

	public AddressFactoryTest() {
		super("AddressFactoryTest");
	}

	protected javax.sip.address.URI createTiURI(String uriString) {
		javax.sip.address.URI uri = null;
		try {
			uri = tiAddressFactory.createURI(uriString);
			assertTrue(uri != null);
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return uri;
	}

	protected javax.sip.address.URI createRiURI(String uriString) {
		javax.sip.address.URI uri = null;
		try {
			uri = riAddressFactory.createURI(uriString);
		} catch (Exception ex) {
			throw new TckInternalError(ex.getMessage());
		}
		return uri;
	}

	protected javax.sip.address.SipURI createTiSipURI(
		String name,
		String address) {
		SipURI sipUri = null;
		try {
			sipUri = tiAddressFactory.createSipURI(name, address);
			assertTrue(sipUri != null);
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return sipUri;
	}

	protected javax.sip.address.SipURI createRiSipURI(
		String name,
		String address) {
		SipURI sipUri = null;
		try {
			sipUri = riAddressFactory.createSipURI(name, address);
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return sipUri;
	}

	protected javax.sip.address.TelURL createTiTelURL(String phoneNumber) {
		TelURL telUrl = null;
		try {
			telUrl = tiAddressFactory.createTelURL(phoneNumber);
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return telUrl;
	}

	protected javax.sip.address.TelURL createRiTelURLFromTiTelURL(
		TelURL tiTelURL) {
		TelURL telUrl = null;
		try {
			// The API has a bug here - there is no way to retrieve the
			// phone-context parameter. This will be fixed in the next release.
			int start = tiTelURL.toString().indexOf(':');
			String phoneNumber = tiTelURL.toString().substring(start+1).trim();
			telUrl = riAddressFactory.createTelURL(phoneNumber);
			telUrl.setGlobal(tiTelURL.isGlobal());
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return telUrl;
	}

	protected javax.sip.address.TelURL createRiTelURL(String phoneNumber) {
		TelURL telUrl = null;
		try {
			telUrl = riAddressFactory.createTelURL(phoneNumber);
		} catch (Exception ex) {
			throw new TckInternalError(ex.getMessage());
		}
		return telUrl;
	}

	protected javax.sip.address.Address createAddress(String address) {
		javax.sip.address.Address addr = null;

		try {
			addr = tiAddressFactory.createAddress(address);
			assertTrue(addr != null);
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return addr;
	}

	protected javax.sip.address.Address createAddress(
		javax.sip.address.URI uri) {
		javax.sip.address.Address addr = null;
		try {
			addr = tiAddressFactory.createAddress(uri);
			assertTrue(
				addr.equals(tiAddressFactory.createAddress(uri.toString())));
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
		return addr;
	}

	protected javax.sip.address.Address createRiAddress(
		javax.sip.address.URI uri) {
		javax.sip.address.Address addr = null;
		try {
			addr = riAddressFactory.createAddress(uri);
		} catch (Exception ex) {
			throw new TckInternalError(ex.getMessage());
		}
		return addr;
	}

	protected javax.sip.address.Address createRiAddress(String address) {
		javax.sip.address.Address addr = null;

		try {
			addr = riAddressFactory.createAddress(address);
			assertTrue(addr != null);
		} catch (Exception ex) {
			throw new TckInternalError(ex.getMessage());
		}
		return addr;
	}

	protected javax.sip.address.Address createRiAddressFromTiAddress(
		Address tiAddress)
		throws TiUnexpectedError {
		try {
			return riAddressFactory.createAddress(tiAddress.toString());
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		}
	}

	public void testAddressFactory() {
		try {

			for (int i = 0; i < urls.length; i++) {
				javax.sip.address.URI uri = this.createTiURI(urls[i]);
				assertTrue(uri != null);
				javax.sip.address.Address tiAddress = this.createAddress(uri);
				assertTrue(tiAddress != null);

				javax.sip.address.URI riUri = this.createRiURI(urls[i]);
				javax.sip.address.Address riAddress =
					this.createRiAddress(riUri);
				tiAddress = this.createRiAddress(tiAddress.toString());

				assertTrue(
					riAddress.equals(
						this.createRiAddressFromTiAddress(tiAddress)));

			}

			for (int i = 0; i < hosts.length; i++) {
				javax.sip.address.SipURI tiSipURI;
				javax.sip.address.SipURI riSipURI;
				tiSipURI = this.createTiSipURI(null, addresses[i]);
				assertTrue(tiSipURI != null);
				assertTrue(tiSipURI.isSipURI());
				assertTrue(!tiSipURI.isSecure());
				assertTrue(
					(
						(SipURI) this.tiAddressFactory.createURI(
							"sip:" + addresses[i])).equals(
						tiSipURI));
				riSipURI = this.createRiSipURI(null, addresses[i]);
				javax.sip.address.Address tiAddress =
					this.createAddress(tiSipURI);
				assertTrue(tiAddress != null);
				javax.sip.address.Address riAddress =
					this.createRiAddress(riSipURI);
				assertTrue(
					riAddress.equals(
						this.createRiAddressFromTiAddress(tiAddress)));

				tiSipURI = this.createTiSipURI("jaintck", addresses[i]);
				assertTrue(tiSipURI != null);
				assertTrue(tiSipURI.isSipURI());
				assertTrue(!tiSipURI.isSecure());
				assertTrue(
					(
						(SipURI) this.tiAddressFactory.createURI(
							"sip:jaintck@" + addresses[i])).equals(
						tiSipURI));
				tiAddress = this.createAddress(tiSipURI);
				assertTrue(tiAddress != null);

				riSipURI = this.createRiSipURI("jaintck", addresses[i]);
				riAddress = this.createRiAddress(riSipURI);
				assertTrue(
					riAddress.equals(
						this.createRiAddressFromTiAddress(tiAddress)));
			}

			for (int i = 0; i < phoneNumbers.length; i++) {
				javax.sip.address.TelURL tiTelUrl =
					this.createTiTelURL(phoneNumbers[i]);
				assertTrue(tiTelUrl != null);
				javax.sip.address.TelURL riTelUrl =
					this.createRiTelURL(phoneNumbers[i]);
				assertTrue(
					riTelUrl.equals(createRiTelURLFromTiTelURL(tiTelUrl)));
			}

			for (int i = 0; i < telUrls.length; i++) {
				javax.sip.address.TelURL telUrl =
					(TelURL) this.createTiURI(telUrls[i]);
				assertTrue(telUrl != null);
			        int start = telUrl.toString().indexOf(':');
			        String phone = telUrl.toString().substring(start+1).trim();
				javax.sip.address.TelURL tiTelUrl = this.createTiTelURL(phone);
				tiTelUrl.setGlobal(telUrl.isGlobal());
				assertTrue(telUrl.equals(tiTelUrl));
			}
		} catch (Exception ex) {
			throw new TiUnexpectedError(ex.getMessage());
		} finally {
			logTestCompleted("testAddressFactory()");
		}

	}

	public void setUp() {

	}

	public static Test suite() {
		return new TestSuite(AddressFactoryTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AddressFactoryTest.class);
	}

}
