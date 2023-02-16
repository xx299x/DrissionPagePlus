# -*- coding:utf-8 -*-
from setuptools import setup, find_packages

with open("README.md", "r", encoding='utf-8') as fh:
    long_description = fh.read()

setup(
    name="DrissionPage",
    version="3.1.4",
    author="g1879",
    author_email="g1879@qq.com",
    description="A module that integrates selenium and requests session, encapsulates common page operations.",
    long_description=long_description,
    long_description_content_type="text/markdown",
    license="BSD",
    keywords="DrissionPage",
    url="https://gitee.com/g1879/DrissionPage",
    include_package_data=True,
    packages=find_packages(),
    zip_safe=False,
    install_requires=[
        "selenium",
        "lxml",
        "tldextract",
        "requests",
        "DownloadKit>=0.5.0",
        "FlowViewer",
        "websocket-client",
        'click~=8.1.3'
    ],
    classifiers=[
        "Programming Language :: Python :: 3.6",
        "Development Status :: 4 - Beta",
        "Topic :: Utilities",
        "License :: OSI Approved :: BSD License",
    ],
    python_requires='>=3.6',
    entry_points={
        'console_scripts': [
            'dp = DrissionPage.cli:main',
        ],
    },
)
