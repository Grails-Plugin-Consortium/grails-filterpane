class BootStrap {

     def init = { servletContext ->
		Author da = new Author(firstName:'Douglas', lastName:'Adams')
		Author cl = new Author(firstName:'Clive', lastName:'Lewis')
		Author ra = new Author(firstName:'Richard', lastName:'Adams')
		Author mt = new Author(firstName:'Mark', lastName:'Twain')
		Author sk = new Author(firstName:'Steve', lastName:'Krug')
        Author sf = new Author(firstName:'Scott', lastName:'Fox')
        Author mg = new Author(firstName:'Malcolm', lastName:'Gladwell')
        Author hm = new Author(firstName:'Herman', lastName:'Melville')
        Author ac = new Author(firstName:'Arthur', lastName:'Clarke')
		
		da.save()
		cl.save()
		ra.save()
		mt.save()
		sk.save()
        sf.save()
        mg.save()
        hm.save()
        ac.save()
		
		new Book(author:cl, title:"The Voyage of the Dawn Treader", releaseDate:java.sql.Date.valueOf('1952-01-01'), inStock:false, price:25.00, cost:20.00, readPriority:'High', bookType:BookType.Fiction).save()
		new Book(author:ra, title:"Watership Down", releaseDate:java.sql.Date.valueOf('1972-01-01'), inStock:true, price:7.99, cost:3.27, readPriority:'Normal', bookType:BookType.Fiction).save()
		new Book(author:mt, title:"The Adventures of Tom Sawyer", releaseDate:java.sql.Date.valueOf('1876-01-01'), inStock:true, price:9.99, cost:4.34, readPriority:'Low', bookType:BookType.Fiction).save()
		new Book(author:mt, title:"The Prince and the Pauper", releaseDate:java.sql.Date.valueOf('1882-01-01'), inStock:false, price:9.99, cost:3.54, readPriority:'Low', bookType:BookType.NonFiction).save()
		new Book(author:da, title:"The Ultimate Hitchhiker's Guide", releaseDate:java.sql.Date.valueOf('2005-01-01'), inStock:true, price:19.99, cost:8.98, readPriority:'Normal', bookType:BookType.Fiction).save()
		new Book(author:sk, title:"Don't Make Me Think", releaseDate:java.sql.Date.valueOf('2006-01-01'), inStock:true, price:35.99, cost:16.99, readPriority:'Normal', bookType:BookType.Reference).save()
        new Book(author:sf, title:"Internet Riches", releaseDate:java.sql.Date.valueOf('2005-01-01'), inStock:true, price:19.99, cost:12.99, readPriority:'Low', bookType:BookType.NonFiction).save()
        new Book(author:mg, title:"Blink: The Power of Thinking Without Thinking", releaseDate:java.sql.Date.valueOf('2007-04-03'), inStock:true, price:10.87, cost:1.99, readPriority:'Normal', bookType:BookType.NonFiction).save()
        new Book(author:cl, title:"The Last Battle", releaseDate:java.sql.Date.valueOf('1952-01-01'), inStock:true, price:25.00, cost:17.44, readPriority:'High', bookType:BookType.Fiction).save()
        new Book(author:cl, title:"Prince Caspian", releaseDate:java.sql.Date.valueOf('1952-01-01'), inStock:false, price:16.99, cost:9.99, readPriority:'Normal', bookType:BookType.Fiction).save()
        new Book(author:hm, title:"Moby Dick", releaseDate:java.sql.Date.valueOf('1851-01-01'), inStock:true, price:9.99, cost:5.25, readPriority:'High', bookType:BookType.Fiction).save()
        new Book(author:ac, title:"The Collected Stories of Arthur C. Clarke", releaseDate:java.sql.Date.valueOf('2002-01-14'), inStock:true, price:13.99, cost:9.99, readPriority:'Normal', bookType:BookType.Fiction).save()
     }
     def destroy = {
     }
} 