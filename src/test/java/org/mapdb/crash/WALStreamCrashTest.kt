package org.mapdb.crash

import org.junit.Test
import org.mapdb.DBUtil
import org.mapdb.crash.CrashJVM
import java.io.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by jan on 3/16/16.
 */
class WALStreamCrashTest: CrashJVM(){

    override fun doInJVM(startSeed: Long, params: String) {
        val f = getTestDir().path+"/aaa"
        val out = FileOutputStream(f)
        val b = ByteArray(8)
        val br = ByteArray(params.toInt())
        val r = Random(0)
        var seed = startSeed
        while(true){
            seed++
            startSeed(seed)
            r.nextBytes(br)
            out.write(br)

            DBUtil.putLong(b, 0, seed)
            out.write(b)
            out.flush()
            commitSeed(seed)
        }

    }

    override fun verifySeed(startSeed: Long, endSeed: Long, params: String): Long {
        val f = getTestDir().path+"/aaa"
        val ins = BufferedInputStream(FileInputStream(f))
        val b = ByteArray(8)
        val br1 = ByteArray(params.toInt())
        val br2 = ByteArray(params.toInt())
        val r = Random(0)
        var lastSeed = 0L
        while(true){
            try{
                DBUtil.readFully(ins, br1)
                r.nextBytes(br2)
                assertTrue(Arrays.equals(br1, br2))

                DBUtil.readFully(ins, b)
                lastSeed = DBUtil.getLong(b,0)
            }catch(e: IOException){
                break
            }
        }
        assertTrue(lastSeed == endSeed || lastSeed == endSeed + 1)

        File(f).delete()
        return endSeed+10
    }

    @Test fun run1(){
        run(this, killDelay = 1000, params = "8")
    }

    @Test fun run2(){
        run(this, killDelay = 1000, params = "100")
    }


    @Test fun run3(){
        run(this, killDelay = 1000, params = "1000")
    }
}
