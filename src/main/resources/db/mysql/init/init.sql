drop table if exists person;
drop table if exists address;

create table person (
    id int auto_increment primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    birth_date date not null,
    employed varchar(3) not null,
    occupation varchar(30) null,
    address_id int not null,
    parent_id int null
);

create table address (
    id int auto_increment primary key,
    street_address varchar(30) not null,
    city varchar(30) not null,
    state char(2) not null
);

insert into address(street_address, city, state) values('금천구', '서울', 'IN');
insert into address(street_address, city, state) values('중구', '서울', 'IN');
insert into address(street_address, city, state) values('동대문구', '서울', 'IN');
insert into address(street_address, city, state) values('서초구', '서울', 'IN');
insert into address(street_address, city, state) values('사하구', '부산', 'IN');
insert into address(street_address, city, state) values('중구', '부산', 'IN');
insert into address(street_address, city, state) values('북구', '부산', 'IN');
insert into address(street_address, city, state) values('남구', '부산', 'IN');

insert into person(first_name, last_name, birth_date, employed, occupation, address_id, parent_id) values('달마', '정', '1935-02-01', 'Yes', 'Brontosaurus Operator', 1, null);
insert into person(first_name, last_name, birth_date, employed, occupation, address_id, parent_id) values('혁거세', '박', '1940-02-01', 'Yes', 'Accountant', 1, null);
insert into person(first_name, last_name, birth_date, employed, address_id, parent_id) values('혁거세2', '박', '1960-05-06', 'No', 1, 2);
insert into person(first_name, last_name, birth_date, employed, occupation, address_id, parent_id) values('길동', '최', '1937-02-01', 'Yes', 'Brontosaurus Operator', 2, null);
insert into person(first_name, last_name, birth_date, employed, occupation, address_id, parent_id) values('무개', '최', '1943-02-01', 'Yes', 'Engineer', 2, null);
insert into person(first_name, last_name, birth_date, employed, address_id, parent_id) values('무개2', '최', '1963-07-08', 'No', 2, 4);
