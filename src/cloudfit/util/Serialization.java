/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.util;

//import de.ruedigermoeller.serialization.FSTConfiguration;
//import de.ruedigermoeller.serialization.FSTObjectInput;
//import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class Serialization {

    // ! reuse this Object, it caches metadata. Performance degrades massively
// if you create a new Configuration Object with each serialization !
//   static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
//public Object readObject(InputStream stream) throws IOException, ClassNotFoundException {
//        //FSTObjectInput in = conf.getObjectInput(stream);
//        FSTObjectInput in = new FSTObjectInput(stream);
//        Object result = in.readObject();
//        in.close();
//        // DON'T: in.close(); here prevents reuse and will result in an exception      
//        //stream.close();
//        return result;
//    }
//
//    public void writeObject(OutputStream stream, Object toWrite) throws IOException {
//        //FSTObjectOutput out = conf.getObjectOutput(stream);
//        FSTObjectOutput out = new FSTObjectOutput(stream);
//        
//        out.writeObject(toWrite);
//        // DON'T out.close() when using factory method;
//        out.flush();
//        out.close();
//        //stream.close();
//    }
//    //Legacy Java serialization
    public Object readObject(InputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(stream);
        Object result = in.readObject();
        return result;
    }
//

    public void writeObject(OutputStream stream, Object toWrite) throws IOException {

        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(toWrite);
        out.flush();
    }
}
