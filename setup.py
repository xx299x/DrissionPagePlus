#!/usr/bin/env python
# -*- coding:utf-8 -*-

from setuptools import setup, find_packages

with open("README.md", "r",encoding='utf-8') as fh:
    long_description = fh.read()

setup(
    name="DrissionPage",
    version="0.8.0",
    author="g1879",
    author_email="g1879@qq.com",
    description="A module that integrates selenium and requests session, encapsulates common page operations, "
                "can achieve seamless switching between the two modes.",
    long_description=long_description,
    license="BSD",
    keywords="DrissionPage",
    url="https://github.com/g1879/DrissionPage",
    packages=find_packages(),
    install_requires=[
        "selenium",
        "requests-html",
        "tldextract",
        "requests"
    ],
    classifiers=[
        "Development Status :: 1 - Alpha",
        "Topic :: Utilities",
        "License :: OSI Approved :: BSD License",
    ],
)
