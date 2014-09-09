package org.dcache.nfs.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcache.nfs.ExportFile;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v3.xdr.READDIR3args;
import org.dcache.nfs.v3.xdr.READDIR3res;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.FileHandle;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.testutils.NfsV3Ops;
import org.dcache.testutils.RpcCallBuilder;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrEncodingStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class NfsServerV3READDIR_3Test {

    private FileHandle dirHandle;
    private Inode dirInode;
    private Stat dirStat;
    private VirtualFileSystem vfs;
    private NfsServerV3 nfsServer;

    @Before
    public void setup() throws Exception {
        dirHandle = new FileHandle(0, 1, 0, new byte[] { 0, 0, 0, 1 }); // the dir we want to read
        dirInode = new Inode(dirHandle);
        dirStat = new Stat(); // the stat marking it as a dir
        //noinspection OctalInteger
        dirStat.setMode(Stat.S_IFDIR | 0755);
        dirStat.setMTime(System.currentTimeMillis());
        vfs = Mockito.mock(VirtualFileSystem.class); // the vfs serving it
        Mockito.when(vfs.getattr(Mockito.eq(dirInode))).thenReturn(dirStat);
        ExportFile exportFile = new ExportFile(this.getClass().getResource("simpleExports")); // same package as us
        nfsServer = new NfsServerV3(exportFile, vfs);
    }

    @Test
    public void testReadDirWithNoResults() throws Exception {
        // vfs will return an empty list from the vfs for dir (technically legal)
        Mockito.when(vfs.list(Mockito.eq(new Inode(dirHandle)))).thenReturn(Collections.<DirectoryEntry> emptyList());

        // set up and execute the call
        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIR3args args = NfsV3Ops.readDir(dirHandle);
        READDIR3res result = nfsServer.NFSPROC3_READDIR_3(call, args);

        Assert.assertEquals(nfsstat.NFS_OK, result.status);
        Assert.assertNull(result.resok.reply.entries); //no entries
        Assert.assertTrue(result.resok.reply.eof); //eof
        assertXdrEncodable(result);
    }

    @Test
    public void testReadDirWithTinyLimit() throws Exception {
        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat));
        Mockito.when(vfs.list(Mockito.eq(dirInode))).thenReturn(dirContents);

        // set up and execute the 1st call - no cookie, but very tight size limit
        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIR3args args = NfsV3Ops.readDir(dirHandle, 10); //10 bytes - not enough for anything
        READDIR3res result = nfsServer.NFSPROC3_READDIR_3(call, args);

        Assert.assertEquals(nfsstat.NFSERR_TOOSMALL, result.status); //error response
    }

    @Test
    public void testContinueReadingAfterEOF() throws Exception {

        // vfs will return only "." and ".." as contents, both leading to itself
        List<DirectoryEntry> dirContents = new ArrayList<>();
        dirContents.add(new DirectoryEntry(".", dirInode, dirStat));
        dirContents.add(new DirectoryEntry("..", dirInode, dirStat));
        Mockito.when(vfs.list(Mockito.eq(dirInode))).thenReturn(dirContents);

        // set up and execute the 1st call - no cookie, but very tight size limit
        RpcCall call = new RpcCallBuilder().from("1.2.3.4", "someHost.acme.com", 42).nfs3().noAuth().build();
        READDIR3args args = NfsV3Ops.readDir(dirHandle);
        READDIR3res result = nfsServer.NFSPROC3_READDIR_3(call, args);

        Assert.assertEquals(nfsstat.NFS_OK, result.status); //response ok
        Assert.assertTrue(result.resok.reply.eof); //eof
        assertXdrEncodable(result);

        // client violates spec - attempts to read more
        // using cookie on last (2nd) entry and returned verifier
        long cookie = result.resok.reply.entries.nextentry.cookie.value.value;
        byte[] cookieVerifier = result.resok.cookieverf.value;
        args = NfsV3Ops.readDir(dirHandle, cookie, cookieVerifier);
        result = nfsServer.NFSPROC3_READDIR_3(call, args);

        Assert.assertEquals(nfsstat.NFSERR_BAD_COOKIE, result.status); //error response
        assertXdrEncodable(result);
    }

    private void assertXdrEncodable (XdrAble xdrAble) {
        try {
            XdrEncodingStream outputStream = Mockito.mock(XdrEncodingStream.class);
            xdrAble.xdrEncode(outputStream); // should not blow up
        } catch (Exception e) {
            throw new AssertionError("object does not survive xdr encoding", e);
        }
    }
}
