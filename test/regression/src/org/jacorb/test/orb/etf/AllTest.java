package org.jacorb.test.orb.etf;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import junit.framework.*;

/**
 * @version $Id$
 */

public class AllTest extends TestCase
{
    public AllTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("ETF Tests");

        suite.addTest(C_WIOP_S_WIOP.suite());
        suite.addTest(C_WIOP_S_IIOP_WIOP.suite());
        suite.addTest(C_IIOP_WIOP_S_IIOP_WIOP.suite());
        suite.addTest(C_IIOP_WIOP_S_WIOP_IIOP.suite());
        suite.addTest(C_WIOP_IIOP_S_WIOP_IIOP.suite());
        suite.addTest(C_IIOP_S_WIOP.suite());
        suite.addTest(ProfileSelectorTest.suite());
        suite.addTest(ServerListenerTest.suite());

        return suite;
    }
}
