package furhatos.app.agent.nlu

import com.google.gson.Gson
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
                "What are my options?",
                "List my options, please",
                "List the options",
                "Can you list the categories again, please?",
                "List the items again.",
                "What could I do, again?")
    }
}

class RequestItemsOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "Can you list the items again, please?",
                "List the items again.",
                "Which items were there?",
                "What could I buy, again?")
    }
}

class RequestCategoriesOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "Can you list the categories again, please?",
                "List the categories again.",
                "What were the categories?")
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

data class CategoryData (
        var name: String,
        var id: Int? = null,
        var items: List<ItemData>? = null
)

data class ItemData(
        var name: String,
        var id: Int? = null,
        var category_id: Int? = null,
        var price: Int? = null
)





/**
 * EnumEntity. All available categories.
 * Currently hardcoded, should be fetched from API.
 */
class Category: EnumEntity(stemming = true, speechRecPhrases = true) {
    fun getNames(aList: List<CategoryData>): List<String>{
        return aList.map { it.name }.toList()
    }

    override fun getEnum(lang: Language): List<String> {
        val targetURL = "http://127.0.0.1:9000/categories"
        val response = khttp.get(targetURL).text
        val categories = Gson().fromJson(response, CategoryList::class.java)
        return getNames( categories )
        // return listOf("tablet", "laptop", "mobile")
    }

}

class CategoryList: ArrayList<CategoryData>()

/**
 * EnumEntity. All available items.
 * Currently hardcoded, should be fetched from API.
 */
class Item: EnumEntity(stemming = true, speechRecPhrases = true) {
    fun getNames(aList: List<ItemData>): List<String> {
        return aList.map { it.name }.toList()
    }

    fun getItems(aList: List<CategoryData>): List<ItemData>{
        val returnList = ArrayList<List<ItemData>>()
        for(data in aList){
            if (data.items != null){
                returnList.add(data.items!!)
            }
        }
        return returnList.flatten()
    }

    override fun getEnum(lang: Language): List<String> {
        val targetURL = "http://127.0.0.1:9000/categories"
        val response = khttp.get(targetURL).text
        val categories = Gson().fromJson(response, CategoryList::class.java)
        println(getNames(getItems(categories)))
        return getNames(getItems(categories))
        // return listOf("Lenovo", "Apple", "Asus")
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
                "What do you have in @category ?",
                "I want a @category",
                "I would like to buy a @category",
                "What @category do you have?")
    }
}

/**
 * Intent. The user wishes to add a specific item to their shopping cart.
 */
class AddItem(var item: QuantifiedItem? = null): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@item",
                "I'll add an @item",
                "@item, please.",
                "Add @item to the shopping cart",
                "I want to buy @item",
                "I would like to buy @item",
                "I'll get @item",
                "I'll have @item",
                "I want an @item")
    }
}

/**
 * Intent. The user wishes to remove a specific item from their shopping cart.
 */
class RemoveItem(var item: Item? = null): Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I don't need @item",
                "I'll remove an @item",
                "Remove the @item from the shopping cart",
                "I don't need the @item",
                "I changed my mind about the @item")
    }
}

class ChangeItem(var item: Item? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@item",
                "I want to change the @item",
                "Let's change an @item",
                "I want to take a look at @item")
    }
}
/**
 * ListEntity. A lists of numerals constitutes a tracking number.
 */
class TrackingNumber: ListEntity<Numeral>()

class ItemsInCart: ListEntity<Item>()

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
                "I would like to check out my shopping cart",
                "I'll go to check out",
                "Check out, please.",
                "I would like to check out my shopping cart",
                "Check out",
                "Checkout")
    }
}

/**
 * Intent. The user is aborting the current operation. The system should return to root state.
 */
class Aborting: Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("Never mind",
                "Abort",
                "Let's pretend this didn't happen",
                "Abort my current order")
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
 * Intent. The user wants to change an existing order. Should only be called from root state.
 */
class TrackOrder: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to track my order",
                "Track order",
                "Track",
                "What's my order status?",
                "I'd like to check my order status",
                "I want to know where my order is",
                "I want to know where my package is")
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
