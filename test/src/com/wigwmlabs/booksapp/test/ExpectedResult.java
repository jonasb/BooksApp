/*
 * Copyright 2011 Jonas Bengtsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wigwmlabs.booksapp.test;

import static com.wigwmlabs.booksapp.test.GoogleApiTestUtilities.toList;

import java.util.List;

import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleLink;
import com.wigwamlabs.googlebooks.GoogleRating;

public class ExpectedResult {
	public static final class Books {
		public static final GoogleBook DragonTattoo = new GoogleBook();
		public static final GoogleBook DragonTattoo_Full = new GoogleBook();
		public static final GoogleBook PippiLongstocking = new GoogleBook();
		public static final GoogleBook Stardust = new GoogleBook();
		public static final GoogleBook UML = new GoogleBook();

		static {
			{
				final GoogleBook b = DragonTattoo;
				b.creators = toList("Stieg Larsson");
				b.dates = toList("2009-06-23");
				b.rating = rating();
				b.descriptions = toList("Forty years after the disappearance of Harriet Vanger from the secluded island owned andinhabited by her powerful family, her uncle, convinced that she had ...");
				b.formats = toList("672 pages", "book");
				b.identifiers = toList("O8GmVV4IGR8C", "ISBN:0307473473", "ISBN:9780307473479");
				b.links = bookLinksForFeed("O8GmVV4IGR8C", "bks6",
						"ACfU3U3eng-sIgptYch9UFcoLWa3-rozJQ", false, false);
				b.publishers = toList("Vintage");
				b.subjects = toList("Fiction");
				b.titles = toList("The Girl with the Dragon Tattoo");
				b.scrub();
			}
			{
				final GoogleBook b = DragonTattoo_Full;
				b.creators = DragonTattoo.creators;
				b.dates = DragonTattoo.dates;
				b.rating = DragonTattoo.rating;
				b.descriptions = toList("An international publishing sensation, Stieg Larsson’s Girl with the Dragon Tattoo combines murder mystery, family saga, love story, and financial intrigue into one satisfyingly complex and entertainingly atmospheric novel.Harriet Vanger, scion of one of Sweden’s wealthiest families, disappeared over forty years ago. All these years later, her aged uncle continues to seek the truth. He hires Mikael Blomkvist, a crusading journalist recently trapped by a libel conviction, to investigate. He is aided by the pierced and tattooed punk prodigy Lisbeth Salander. Together they tap into a vein of unfathomable iniquity and astonishing corruption.From the Trade Paperback edition.");
				b.formats = toList("Dimensions 10.6x18.0x3.5 cm", "658 pages", "book");
				b.identifiers = DragonTattoo.identifiers;
				b.links = bookLinksForBook("O8GmVV4IGR8C", "bks6",
						"ACfU3U3eng-sIgptYch9UFcoLWa3-rozJQ", false);
				b.publishers = toList("Vintage Books");
				b.subjects = toList("Computer hackers", "Corruption/ Sweden/ Fiction",
						"Corruption", "Corruption", "Finance", "Journalists",
						"Missing persons/ Sweden/ Fiction", "Missing persons",
						"Rich people/ Sweden/ Fiction", "Rich people", "Stockholm (Sweden)",
						"Suspense fiction", "Fiction / General",
						"Fiction / Mystery & Detective / General", "Fiction / Suspense",
						"Fiction / Thrillers", "Fiction / Political", "Fiction / Suspense");
				b.titles = DragonTattoo.titles;
				b.scrub();
			}
			{
				final GoogleBook b = PippiLongstocking;
				b.creators = toList("Astrid Lindgren");
				b.dates = toList("2003");
				b.formats = toList("123 pages", "book");
				b.identifiers = toList("qAlBAQAACAAJ", "ISBN:9129657520", "ISBN:9789129657524");
				b.links = bookLinksForFeed("qAlBAQAACAAJ", "bks7",
						"ACfU3U1vHvGAHEgLsjpqZxey5B1hg1CULw", false, false);
				b.titles = toList("Pippi L\u00e5ngstrump i S\u00f6derhavet");
				b.scrub();
			}
			{
				final GoogleBook b = Stardust;
				b.creators = toList("Neil Gaiman");
				b.dates = toList("2008-12-15");
				b.rating = rating();
				b.descriptions = toList("Tristan Thorne, who lives in the quiet Victorian countryside town of Wall, crosses intothe world of Faerie to recover a fallen star for the woman he loves, but ...");
				b.formats = toList("288 pages", "book");
				b.identifiers = toList("5fIIAbklilkC", "ISBN:0061689246", "ISBN:9780061689246");
				b.links = bookLinksForFeed("5fIIAbklilkC", "bks1",
						"ACfU3U2XfV37cPZeqwyxtVCL33ZCN5HB6Q", false, false);
				b.publishers = toList("Harpercollins Childrens Books");
				b.subjects = toList("Fiction");
				b.titles = toList("Stardust");
				b.scrub();
			}
			{
				final GoogleBook b = UML;
				b.creators = toList("Per Kroll", "Philippe Kruchten");
				b.dates = toList("2003-04-18");
				b.rating = rating();
				b.descriptions = toList("The authors explain the underlying software development principles behind theRUP, and guide readers in its application in their organization.");
				b.formats = toList("416 pages", "book");
				b.identifiers = toList("7FSf5661dfMC", "ISBN:0321166094", "ISBN:9780321166098");
				b.links = bookLinksForFeed("7FSf5661dfMC", "bks0",
						"ACfU3U0jG07nNJTn3KVseYGjNJaIuDGHkw", true, true);
				b.publishers = toList("Addison-Wesley Professional");
				b.subjects = toList("Computers");
				b.titles = toList("The rational unified process made easy",
						"a practitioner's guide to the RUP");
				b.scrub();
			}
		}

		private static GoogleLink alternateLink(final String googleId, boolean includeQuery) {
			return link("alternate", "http://books.google.com/books?id=" + googleId
					+ (includeQuery ? "&dq=SEARCH_QUERY" : "") + "&ie=ISO-8859-1");
		}

		private static GoogleLink annotationLink() {
			return link("http://schemas.google.com/books/2008/annotation",
					"http://www.google.com/books/feeds/users/me/volumes");
		}

		private static List<GoogleLink> bookLinksForBook(final String googleId,
				final String thumbnailServer, final String thumbnailSig, boolean curl) {
			return toList(thumbnailLink(googleId, thumbnailServer, thumbnailSig, curl),
					infoLink(googleId, false), annotationLink(), alternateLink(googleId, false),
					selfLink(googleId));
		}

		private static List<GoogleLink> bookLinksForFeed(final String googleId,
				final String thumbnailServer, final String thumbnailSig, boolean curl,
				boolean previewFrontCover) {
			return toList(
					thumbnailLink(googleId, thumbnailServer, thumbnailSig, curl),
					infoLink(googleId, true),
					link("http://schemas.google.com/books/2008/preview",
							"http://books.google.com/books?id=" + googleId
									+ (previewFrontCover ? "&printsec=frontcover" : "")
									+ "&dq=SEARCH_QUERY&ie=ISO-8859-1&cd=X&source=gbs_gdata"),
					annotationLink(), alternateLink(googleId, true), selfLink(googleId));
		}

		private static GoogleLink infoLink(final String googleId, boolean includeQuery) {
			return link("http://schemas.google.com/books/2008/info",
					"http://books.google.com/books?id=" + googleId
							+ (includeQuery ? "&dq=SEARCH_QUERY" : "")
							+ "&ie=ISO-8859-1&source=gbs_gdata");
		}

		private static GoogleLink link(String rel, String href) {
			final GoogleLink l = new GoogleLink();
			l.rel = rel;
			l.href = href;
			return l;
		}

		private static GoogleRating rating() {
			final GoogleRating r = new GoogleRating();
			r.average = "3.5"; // unreliable
			r.max = "5";
			return r;
		}

		private static GoogleLink selfLink(final String googleId) {
			return link("self", "http://www.google.com/books/feeds/volumes/" + googleId);
		}

		private static GoogleLink thumbnailLink(final String googleId,
				final String thumbnailServer, final String thumbnailSig, boolean curl) {
			return link("http://schemas.google.com/books/2008/thumbnail", "http://"
					+ thumbnailServer + ".books.google.com/books?id=" + googleId
					+ "&printsec=frontcover&img=1&zoom=5" + (curl ? "&edge=curl" : "") + "&sig="
					+ thumbnailSig + "&source=gbs_gdata");
		}
	}
}
