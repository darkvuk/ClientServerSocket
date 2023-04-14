# ClientServerSocket

U programskom jeziku Java, korišćenjem soketa, implementirana su dva procesa (klijent i
server) koji mogu razmjenjivati:

* tekstualne poruke.
* slike u JPG ili PNG formatu.
 
U sklopu serverskog procesa otvoren je server soket na portu 13. Kreirana su
dva foldera za skladištenje slika, po jedan za servera i klijenta.
<br />
<br />
### Protokol komunikacije
1) Klijent šalje zahtjev za konekcijom ka odabranom portu na serveru.
2) Server prihvata konekciju i otvara soket za komunikaciju.
3) Klijentu se na konzoli štampa prompt „Klijent: “ i čeka se na unos poruke. Klijent u
konzolu unosi tekst poruke ili putanju do slike koju želi da pošalje. Putanja će se
od običnog teksta razlikovati po tome što se završava sa „.jpg“ ili „.png“.
Preko soketa se šalju poruke u sljedećem formatu:
<br /> <br /> Prvi red poruke je string „TEXT“ ukoliko se šalje tekstualna poruka, a
„IMAGE“ ukoliko se šalje slika.
<br /> <br /> Drugi red tekstualne poruke je prazan, dok je za sliku to ime fajla (samo
ime, ne putanja).
<br /> <br /> U trećem redu poruke je njen sadržaj (bajti teksta ondnosno slike).
4) Server prima poruku i na svojoj konzoli štampa prompt „Klijent: “ a zatim i tekst
poruke. Ukoliko je poslata slika, server štampa ime fajla (drugi red poruke) i
skladišti dobijenu sliku u svom folderu. Zatim se na konzoli servera štampa prompt
„Server: “ i čeka se na unos odgovora. Odgovor takođe može biti tekstualna poruka
ili putanja do slike. Format poruke koja se šalje isti je kao za klijenta.

5) Na klijentskoj strani se po prijemu poruke štampa prompt „Server: “ i tekst poruke
sa servera. U slučaju slike, štampa se ime fajla. Ukoliko je poslata slika, klijent je
skladišti u svom folderu. Zatim se vraća na korak 3).

6) Ovaj postupak se nastavlja sve dok klijent u svojoj konzoli ne unese riječ „quit“,
nakon čega se soket zatvara.
