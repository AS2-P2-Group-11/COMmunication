package furhatos.app.comvocab.nlu

import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.util.Language
import furhatos.nlu.common.Number


class RequestOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What options do you have?",
                "What can I do?",
                "What are the alternatives?",
                "What are my options?")
    }
}

class Add: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'll add something",
                "I would like to add to my order",
                "Add",
                "I'll add, please")
    }
}

class Remove: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'll remove something",
                "I would like to remove my order",
                "Remove",
                "I want to remove, please")
    }
}

class Category: EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        //return FastAPI.get(categories)
        return listOf("Computer", "Tablet", "Mobile")
    }
}

class Item: EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        //return FastAPI.get(items)
        return listOf("Lenovo", "Asus", "Apple")
    }
}

class ChooseCategory(var category: Category? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@category",
                "Something from @category",
                "What do you have in @category ?")
    }
}

class QuantifiedItem(
        val count : Number? = Number(1),
        val item : Item? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @item", "@item")
    }

    override fun toText(): String {
        return generate("$count $item")
    }
}

class AddItem(var item: QuantifiedItem): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@item",
                "I'll add an @item",
                "@item, please.",
                "Add @item to the shopping cart")
    }
}

class RemoveItem(var item: QuantifiedItem): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I don't need @item",
                "I'll remove an @item",
                "Remove the @item from the shopping cart",
                "I don't need the @item",
                "I changed my mind about the @item")
    }
}

class TrackingNumber: ListEntity<Numeral>()

class Numeral(val num: Number? = Number(0)): ComplexEnumEntity(){
    override fun getEnum(lang: Language): List<String> {
        return listOf("@num")
    }

    override fun toText(): String {
        return generate("$num")
    }
}

class InputTrackingNumber(var trackingNumber: TrackingNumber? = null): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@trackingNumber",
                "It's @trackingNumber",
                "The tracking number is @trackingNumber")
    }
}

class Checkout: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'm done shopping",
                "I'll go to checkout",
                "Checkout, please.",
                "I would like to check out my shopping cart")
    }
}

class Abort: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("Never mind",
                "Abort",
                "Let's pretend this didn't happen")
    }
}

class GoodBye: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("Good bye",
                "Thanks for all your help",
                "That will be all",
                "Goodbye",
                "I'm leaving")
    }
}

class CheckShoppingCart: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("What's in my shopping cart?",
                "Check shopping cart",
                "What do I have so far?")
    }
}

class PlaceOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
    return listOf("I'd like to place an order",
            "Place an order",
            "Place")
    }
}

class CancelOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to cancel my order",
                "Cancel an order",
                "Cancel")
    }
}

class ChangeOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to change my order",
                "Modify an order",
                "Change")
    }
}

class CheckStatus: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What's my order status?",
                "I'd like to check my order status",
                "I want to know where my order is",
                "I want to know where my package is")
    }

}