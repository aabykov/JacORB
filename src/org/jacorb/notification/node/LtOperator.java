/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.notification.node;

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.Token;

/** A simple node to represent LT operation */
public class LtOperator extends AbstractTCLNode {

    public LtOperator(Token tok) {
        super(tok);
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws DynamicTypeException,
               EvaluationException {

        EvaluationResult _left, _right;

        _left = left().evaluate(context);
        _right = right().evaluate(context);

        int _comp = _left.compareTo( _right);

        if (_comp == 1 || _comp == 0) {
            return EvaluationResult.BOOL_FALSE;
        }
        return EvaluationResult.BOOL_TRUE;
    }

    public String toString() {
        return "<";
    }

    public String getName() {
        return getClass().getName();
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptInOrder(visitor);
        visitor.visitLt(this);
        right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitLt(this);
        left().acceptPreOrder(visitor);
        right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptPostOrder(visitor);
        right().acceptPostOrder(visitor);
        visitor.visitLt(this);
    }
}
