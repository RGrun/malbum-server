--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: admins; Type: TABLE; Schema: public; Owner: malbum; Tablespace: 
--

CREATE TABLE admins (
    user_id integer NOT NULL
);


ALTER TABLE admins OWNER TO malbum;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: malbum; Tablespace: 
--

CREATE TABLE comments (
    comment_id integer NOT NULL,
    photo_id integer NOT NULL,
    user_id integer NOT NULL,
    comment text NOT NULL,
    date timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false
);


ALTER TABLE comments OWNER TO malbum;

--
-- Name: comments_comment_id_seq; Type: SEQUENCE; Schema: public; Owner: malbum
--

CREATE SEQUENCE comments_comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE comments_comment_id_seq OWNER TO malbum;

--
-- Name: comments_comment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: malbum
--

ALTER SEQUENCE comments_comment_id_seq OWNED BY comments.comment_id;


--
-- Name: photos; Type: TABLE; Schema: public; Owner: malbum; Tablespace: 
--

CREATE TABLE photos (
    photo_id integer NOT NULL,
    user_id integer NOT NULL,
    photo_path text NOT NULL,
    upload_date timestamp with time zone NOT NULL,
    modified_date timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false,
    name text NOT NULL,
    thumb_name text,
    description text DEFAULT 'No comment.'::text,
    custom_name text
);


ALTER TABLE photos OWNER TO malbum;

--
-- Name: photos_photo_id_seq; Type: SEQUENCE; Schema: public; Owner: malbum
--

CREATE SEQUENCE photos_photo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE photos_photo_id_seq OWNER TO malbum;

--
-- Name: photos_photo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: malbum
--

ALTER SEQUENCE photos_photo_id_seq OWNED BY photos.photo_id;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: malbum; Tablespace: 
--

CREATE TABLE settings (
    site_name text NOT NULL,
    super_user_id integer NOT NULL,
    allow_uploads boolean DEFAULT true,
    site_public boolean DEFAULT false,
    anon_comments boolean DEFAULT false
);


ALTER TABLE settings OWNER TO malbum;

--
-- Name: users; Type: TABLE; Schema: public; Owner: malbum; Tablespace: 
--

CREATE TABLE users (
    user_id integer NOT NULL,
    uname text NOT NULL,
    uname_lower text NOT NULL,
    fname text NOT NULL,
    lname text NOT NULL,
    pass text NOT NULL,
    api_key text NOT NULL
);


ALTER TABLE users OWNER TO malbum;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: malbum
--

CREATE SEQUENCE users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE users_user_id_seq OWNER TO malbum;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: malbum
--

ALTER SEQUENCE users_user_id_seq OWNED BY users.user_id;


--
-- Name: comment_id; Type: DEFAULT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY comments ALTER COLUMN comment_id SET DEFAULT nextval('comments_comment_id_seq'::regclass);


--
-- Name: photo_id; Type: DEFAULT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY photos ALTER COLUMN photo_id SET DEFAULT nextval('photos_photo_id_seq'::regclass);


--
-- Name: user_id; Type: DEFAULT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY users ALTER COLUMN user_id SET DEFAULT nextval('users_user_id_seq'::regclass);


--
-- Name: comments_pkey; Type: CONSTRAINT; Schema: public; Owner: malbum; Tablespace: 
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (comment_id);


--
-- Name: photos_pkey; Type: CONSTRAINT; Schema: public; Owner: malbum; Tablespace: 
--

ALTER TABLE ONLY photos
    ADD CONSTRAINT photos_pkey PRIMARY KEY (photo_id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: malbum; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: admins_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY admins
    ADD CONSTRAINT admins_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: comments_photo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_photo_id_fkey FOREIGN KEY (photo_id) REFERENCES photos(photo_id) ON DELETE CASCADE;


--
-- Name: photos_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY photos
    ADD CONSTRAINT photos_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: settings_super_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: malbum
--

ALTER TABLE ONLY settings
    ADD CONSTRAINT settings_super_user_id_fkey FOREIGN KEY (super_user_id) REFERENCES users(user_id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

