/**
 * Implements the CFML Function refind
 */
package railo.runtime.functions.string;

import org.apache.oro.text.regex.MalformedPatternException;

import railo.runtime.PageContext;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.FunctionException;
import railo.runtime.exp.PageException;
import railo.runtime.functions.BIF;
import railo.runtime.op.Caster;
import railo.runtime.regex.Perl5Util;
import railo.runtime.type.Array;

public final class REMatch extends BIF {

	private static final long serialVersionUID = 5332412717334906052L;

	public static Array call(PageContext pc , String regExpr, String str) throws ExpressionException {
		try {
			return Perl5Util.match(regExpr, str, 1, true);
		} 
		catch (MalformedPatternException e) {
			throw new FunctionException(pc,"REMatch",1,"regularExpression",e.getMessage());
		}
	}

    @Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
    	if(args.length==2)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
    	
		throw new FunctionException(pc, "REMatch", 2, 2, args.length);
	}
}