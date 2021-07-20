package freedompay.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

/**
 *
 * This class accepts Item objects and builds an array which
 * is passed to the Receipt class or parsed into JSON/XML.
 *
 * This class is called by FPPaymentDevice and Receipt classes.
 *
 * @author Hunter Yavitz - 6/10/21
 *
 */

@Data
public class Items {

    @JacksonXmlElementWrapper(localName = "Item", useWrapping = false)
    @JsonProperty("Item")
    public Item[] items;

    public Items(){
    }

    public Items(Item[] items) {
        this.items = items;
    }
}