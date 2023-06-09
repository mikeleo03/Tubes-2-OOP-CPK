package main.Plugin.Currency;

import java.io.Serializable;
import javax.xml.bind.annotation.*;

@XmlRootElement
public class Currency implements Serializable {
    // ATTRIBUTES
    private String currencyName;
    private Double currencyRate;

    // CONSTRUCTOR
    public Currency() {
        this.currencyName = "";
        this.currencyRate = 0.0;
    }

    public Currency(String name, Double rate) {
        this.currencyName = name;
        this.currencyRate = rate;
    }

    // GETTER-SETTER
    @XmlElement
    public String getCurrencyName() {
        return this.currencyName;
    }
    public void setCurrencyName(String name) {
        this.currencyName = name;
    }

    @XmlElement
    public Double getCurrencyRate() {
        return this.currencyRate;
    }
    public void setCurrencyRate(Double rate) {
        this.currencyRate = rate;
    }
}
