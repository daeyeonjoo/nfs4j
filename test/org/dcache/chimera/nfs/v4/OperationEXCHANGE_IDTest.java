/*
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.chimera.nfs.v4;

import java.util.UUID;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.v4.client.CreateSessionStub;
import org.dcache.chimera.nfs.v4.client.ExchangeIDStub;
import org.dcache.chimera.nfs.v4.xdr.*;
import org.junit.Before;
import org.junit.Test;

public class OperationEXCHANGE_IDTest {

    private NFSv4StateHandler stateHandler;
    private final String domain = "nairi.desy.de";
    private final String name = "dCache.ORG java based client";
    private String clientId;

    @Before
    public void setUp() {
        stateHandler = new NFSv4StateHandler();
        clientId = UUID.randomUUID().toString();
    }

    @Test
    public void testFreshExchangeId() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = ExchangeIDStub.normal(domain, name, clientId, 0, state_protect_how4.SP4_NONE);
        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);
    }

    @Test
    public void testResendUnconfirmed() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = ExchangeIDStub.normal(domain, name, clientId, 0, state_protect_how4.SP4_NONE);
        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);
        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);
    }

    @Test
    public void testResendConfirmed() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = ExchangeIDStub.normal(domain, name, clientId, 0, state_protect_how4.SP4_NONE);
        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);

        nfs_argop4 cretaesession_args = CreateSessionStub.standard(
                result.opexchange_id.eir_resok4.eir_clientid, result.opexchange_id.eir_resok4.eir_sequenceid);

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CREATE_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .build();

         AssertNFS.assertNFS(CREATE_SESSION, context, result, nfsstat.NFS_OK);
         result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
         AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);
    }

    @Test
    public void testResendConfirmedReboot() throws Exception {
        CompoundContext context;
        nfs_resop4 result;

        nfs_argop4 exchangeid_args = ExchangeIDStub.normal(domain, name, clientId, 0, state_protect_how4.SP4_NONE);
        OperationEXCHANGE_ID EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_args, 0);

        result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .build();

        AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);

        nfs_argop4 cretaesession_args = CreateSessionStub.standard(
                result.opexchange_id.eir_resok4.eir_clientid, result.opexchange_id.eir_resok4.eir_sequenceid);

        OperationCREATE_SESSION CREATE_SESSION = new OperationCREATE_SESSION(cretaesession_args);
        result = nfs_resop4.resopFor(nfs_opnum4.OP_CREATE_SESSION);
        context = new CompoundContextBuilder()
                .withStateHandler(stateHandler)
                .withOpCount(1)
                .build();

         AssertNFS.assertNFS(CREATE_SESSION, context, result, nfsstat.NFS_OK);

         nfs_argop4 exchangeid_reboot_args = ExchangeIDStub.normal(domain, name, clientId, 0, state_protect_how4.SP4_NONE);
         EXCHANGE_ID = new OperationEXCHANGE_ID(exchangeid_reboot_args, 0);
         result = nfs_resop4.resopFor(nfs_opnum4.OP_EXCHANGE_ID);
         AssertNFS.assertNFS(EXCHANGE_ID, context, result, nfsstat.NFS_OK);
    }
}
