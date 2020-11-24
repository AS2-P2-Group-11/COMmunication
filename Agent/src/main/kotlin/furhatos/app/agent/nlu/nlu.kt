package furhatos.app.comvocab.nlu

import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.util.Language
import furhatos.nlu.common.Number

/**
 * Intent. The user wants to know what options are currently available.
 *
 * The response should be context sensitive.
 */
class RequestOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What options do you have?",
                "What can I do?",
                "What are the alternatives?",
                "What are my options?")
    }
}

/**
 * Intent. The system has asked whether something should be added or removed. The user indicates "Add"
 */
class Add: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'll add something",
                "I would like to add to my order",
                "Add",
                "I'll add, please")
    }
}

/**
 * Intent. The system has asked whether something should be added or removed. The user indicates "Remove"
 */

class Remove: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'll remove something",
                "I would like to remove my order",
                "Remove",
                "I want to remove, please")
    }
}

/**
 * EnumEntity. All available categories.
 * Currently hardcoded, should be fetched from API.
 */
class Category: EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        //return FastAPI.get(categories)
        return listOf("Computer", "Tablet", "Mobile")
    }
}

/**
 * EnumEntity. All available items.
 * Currently hardcoded, should be fetched from API.
 */
class Item: EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        //return FastAPI.get(items)
        return listOf("Lenovo", "Asus", "Apple")
    }
}

/**
 * EnumEntity. Numbered items, see Fruit Seller example.
 */
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


/**
 * Intent. The user is choosing which category to find item from.
 */
class ChooseCategory(var category: Category? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@category",
                "Something from @category",
                "What do you have in @category ?")
    }
}

/**
 * Intent. The user wishes to add a specific item to their shopping cart.
 */
class AddItem(var item: QuantifiedItem): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@item",
                "I'll add an @item",
                "@item, please.",
                "Add @item to the shopping cart")
    }
}

/**
 * Intent. The user wishes to remove a specific item from their shopping cart.
 */
class RemoveItem(var item: QuantifiedItem): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I don't need @item",
                "I'll remove an @item",
                "Remove the @item from the shopping cart",
                "I don't need the @item",
                "I changed my mind about the @item")
    }
}

/**
 * ListEntity. A lists of numerals constitutes a tracking number.
 */
class TrackingNumber: ListEntity<Numeral>()

/**
 * EnumEntity. Could probably be done easier, but this is a way to make numerals concrete.
 */
class Numeral(val num: Number? = Number(0)): ComplexEnumEntity(){
    override fun getEnum(lang: Language): List<String> {
        return listOf("@num")
    }

    override fun toText(): String {
        return generate("$num")
    }
}

/**
 * Intent. The user is inputting a tracking number into the system upon request.
 */
class InputTrackingNumber(var trackingNumber: TrackingNumber? = null): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@trackingNumber",
                "It's @trackingNumber",
                "The tracking number is @trackingNumber")
    }
}

/**
 * Intent. The user is checking out their shopping cart.
 */
class Checkout: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'm done shopping",
                "I'll go to checkout",
                "Checkout, please.",
                "I would like to check out my shopping cart")
    }
}

/**
 * Intent. The user is aborting the current operation. The system should return to root state.
 */
class Abort: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("Never mind",
                "Abort",
                "Let's pretend this didn't happen")
    }
}

/**
 * Intent. The user indicates that they are leaving the system.
 */
class GoodBye: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("Good bye",
                "Thanks for all your help",
                "That will be all",
                "Goodbye",
                "I'm leaving")
    }
}

/**
 * Intent. The user wishes to check the contents of their shopping cart.
 */
class CheckShoppingCart: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("What's in my shopping cart?",
                "Check shopping cart",
                "What do I have so far?")
    }
}

/**
 * Intent. The user wants to place a new order. Should only be called from root state.
 */
class PlaceOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
    return listOf("I'd like to place an order",
            "Place an order",
            "Place")
    }
}

/**
 * Intent. The user wants to cancel an existing order. Should only be called from root state.
 */
class CancelOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to cancel my order",
                "Cancel an order",
                "Cancel")
    }
}

/**
 * Intent. The user wants to change an existing order. Should only be called from root state.
 */
class ChangeOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to change my order",
                "Modify an order",
                "Change")
    }
}

/**
 * Intent. The user wants to check the status of an existing order. Should only be called from root state.
 */
class CheckStatus: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What's my order status?",
                "I'd like to check my order status",
                "I want to know where my order is",
                "I want to know where my package is")
    }

}