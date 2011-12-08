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

import java.io.IOException;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationGETDEVICEINFO extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationGETDEVICEINFO.class);

    OperationGETDEVICEINFO(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_GETDEVICEINFO);
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) throws IOException {

        /*
         * GETDEVICEINFO. returns the mapping of device ID to storage device
         * address.
         */
        final GETDEVICEINFO4res res = result.opgetdeviceinfo;

        deviceid4 deviceId = _args.opgetdeviceinfo.gdia_device_id;

        _log.debug("             Info for #{}", deviceId);
        _log.debug("             type for #{}",
                _args.opgetdeviceinfo.gdia_layout_type);

        res.gdir_resok4 = new GETDEVICEINFO4resok();

        device_addr4 deviceInfo = context.getDeviceManager().getDeviceInfo(context, deviceId);

        if (deviceInfo == null) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "invalid deviceInfo id");
        }

        res.gdir_resok4.gdir_device_addr = deviceInfo;
        res.gdir_resok4.gdir_device_addr.da_layout_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
        res.gdir_resok4.gdir_notification = new bitmap4();
        res.gdir_resok4.gdir_notification.value = new uint32_t[1];
        res.gdir_resok4.gdir_notification.value[0] = new uint32_t(0);

        res.gdir_status = nfsstat.NFS_OK;
    }
}
