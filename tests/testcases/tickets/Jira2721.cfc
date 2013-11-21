component extends="org.railo.cfml.test.RailoTestCase"	{


	addrValid = [
		  'someone@domain.com'
		, 'Some One <someone@domain.com>'
		, '"Some One" <someone@domain.com>'
		, '(Some One) someone@domain.com'		
		, 'someone@zurich.ch'		
		, 'someone@zÃ¼rich.ch'
		, '<someone@zÃ¼rich.ch> "zyz 09812)(*"'
		, '<someone@zÃ¼rich.ch> "Some One"'
		, '<someone@zÃ¼rich.ch> Some One'
		, '<someone@zÃ¼rich.ch> (Some One)'
		, 'Some One <someone@zÃ¼rich.ch>'
		, '"Some One" <someone@zÃ¼rich.ch>'
		, '(Some One) someone@zÃ¼rich.ch'
		, 'niceandsimple@example.com'
		, 'very.common@example.com'
		, 'not~that~common@example.com'
		, 'a.little.lengthy.but.fine@dept.example.com'
		, 'disposable.style.email.with+symbol@example.com'
		, '"much.more unusual"@example.com'
		, '" "@example.org'
		, 'weird."but".right@example.com'
		, 'user+tags@example.com'
	];

	addrValidUnused = [		// valid per the spec, but currently not tested; added in case we want to allow these in the future

		  'user@192.168.0.1'
		, 'user@[IPv6:2001:db8:1ff::a0b:dbd0]'

		, '"very.unusual.@.unusual.com"@example.com'
		, '"very.(),:;<>[]\".VERY.\"very@\\ \"very\".unusual"@strange.example.com'
		, 'postbox@com (top-level domains are valid hostnames)'
		, 'admin@mailserver1 (local domain name with no TLD)'
		, "!##$%&'*+-/=?^_`{}|~@example.org"
		, '"()<>[]:,;@\\\"!##$%&''*+-/=?^_`{}| ~.a"@example.org'
	];

	addrInvalid = [

		  'user'
		, 'user@'
		, 'user@.'
		, '.michael@getrailo.org'
		, 'michael.@getrailo.org'
		, 'michael@getrailo.org.'
		, 'michael@.getrailo.org'
		, 'michael@getrailo..org'
		, 'Abc.example.com'
		, 'A@b@c@example.com'
		, 'a"b(c)d,e:f;g<h>i[j\k]l@example.com'
		, 'this is"not\allowed@example.com'
		, 'this\ still\"not\\allowed@example.com'
		, 'Some One someone@zÃ¼rich.ch'
		, '<someone@zÃ¼rich.ch> "zyz 09812)(*'
		, '<someone@zÃ¼rich.ch> zyz 09812)(*'
		, 'user@IPv6:2001:db8:1ff::a0b:dbd0'
		, 'user@2001:db8:1ff::a0b:dbd0'

		// , 'just"not"right@example.com'
	];


	public function testSingle() {

		var addr = '"Some One" <someone@domain.com>';

		addr = 'user@getrailo.or';

		assert( isValid("email", addr), "validation for [#addr#] failed" );
	}


	public function testValidEmails() {

		for ( var addr in variables.addrValid ) {

			assert( isValid("email", addr), "validation for [#addr#] failed - expected true." );
		}
	}


	public function testInvalidEmails() {

		for ( var addr in variables.addrInvalid ) {

			assert( !isValid("email", addr), "validation for [#addr#] failed - expected false." );
		}
	}


}